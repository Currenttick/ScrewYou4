package fr.geming400.screwyou4.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.geming400.screwyou4.killer.AssignedSentence;
import fr.geming400.screwyou4.killer.ClassExecutioner;
import fr.geming400.screwyou4.killer.Sentence;
import fr.geming400.screwyou4.killer.parser.ClassRepresentation;
import fr.geming400.screwyou4.killer.parser.Infos;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class SentenceArgumentType implements ArgumentType<AssignedSentence> {

    private String cachedClassName;
    private ClassRepresentation cachedClassRepresentation;
    private String cachedMethodName;
    private Infos.MethodInfo cachedMethodInfo;
    private boolean isOverridingReturn;
    private boolean containsMethod;

    @SuppressWarnings({"ResultOfMethodCallIgnored", "AssignmentUsedAsCondition"})
    @Override
    public AssignedSentence parse(StringReader reader) throws CommandSyntaxException {
        String[] strings = reader.getRemaining().split(" ");
        boolean inQuotations = false;
        while (reader.canRead() && (inQuotations || reader.peek() != ' ')) {
            if (reader.read() == '"') {
                inQuotations = !inQuotations;
            }
        }
        StringBuilder argument = new StringBuilder(strings[0]);
        if (argument.toString().contains("\"") && argument.toString().lastIndexOf('"') == argument.toString().indexOf('"')) {
            int i = 1;
            while (i < strings.length && !strings[i].contains("\"")) {
                argument.append(' ').append(strings[i++]);
            }
            argument.append(' ').append(strings[i]);
        }
        String className;
        final String methodName;
        String overriddenReturn = null;
        if (this.containsMethod = argument.toString().contains("#")) {
            // has method in it
            String[] argument2 = argument.toString().split("#");
            className = argument2[0];
            if (argument2.length > 1) {
                if (this.isOverridingReturn = argument2[1].contains("=")) {
                    // Has an overridden return value
                    String[] argument3 = argument2[1].split("=");
                    methodName = argument3[0];
                    if (methodName.equals("*")) {
                        throw SentenceArgumentTypeSyntaxException.build("You cannot use '*' with return value overriding!");
                    }
                    if (argument3.length == 2) {
                        overriddenReturn = argument3[1];
                    }
                } else {
                    methodName = argument2[1];
                }
            } else {
                methodName = null;
            }
        } else {
            className = argument.toString();
            methodName = null;
        }
        Class<?> clazz;
        if (!className.equals(this.cachedClassName)) {
            this.cachedClassName = className;
            if (argument.toString().contains("#")) {
                clazz = this.updateClassRepresentation();
            } else {
                this.cachedClassRepresentation = null;
                clazz = this.getClassFromCachedName();
            }
        } else {
            clazz = this.getClassFromCachedName();
        }
        if (!Objects.equals(methodName, this.cachedMethodName) && !"*".equals(methodName)) {
            this.cachedMethodName = methodName;
            if (methodName != null) {
                this.cachedMethodInfo = this.cachedClassRepresentation.getMethodInfos().stream().filter(mi -> mi.getShortName().equals(methodName)).findFirst().orElseThrow(() -> SentenceArgumentTypeSyntaxException.build("Method not found"));
            }
        }
        if (overriddenReturn != null && !overriddenReturn.isEmpty()) {
            switch (this.cachedMethodInfo.getReturnType()) {
                case "V" -> throw SentenceArgumentTypeSyntaxException.build("Method does not have a return value!");
                case "Z" -> {
                    if (!(overriddenReturn.equals("true") || overriddenReturn.equals("false"))) {
                        throw SentenceArgumentTypeSyntaxException.build(overriddenReturn + " is not a valid boolean!");
                    }
                }
                case "B" -> Byte.parseByte(overriddenReturn);
                case "S" -> Short.parseShort(overriddenReturn);
                case "C" -> {
                    if (overriddenReturn.length() > 1) {
                        throw SentenceArgumentTypeSyntaxException.build(overriddenReturn + " is not a valid character!");
                    }
                }
                case "I" -> Integer.parseInt(overriddenReturn);
                case "J" -> Long.parseLong(overriddenReturn);
                case "F" -> Float.parseFloat(overriddenReturn);
                case "D" -> Double.parseDouble(overriddenReturn);
                case "Ljava/lang/String;" -> {
                    if (overriddenReturn.charAt(0) != '"' || !overriddenReturn.endsWith("\"")) {
                        throw SentenceArgumentTypeSyntaxException.build("You must quote with \" the string!");
                    }
                    // Remove quotations
                    overriddenReturn = overriddenReturn.substring(1, overriddenReturn.length() - 1);
                }
                default -> {
                    if (!overriddenReturn.equals("null")) {
                        if (overriddenReturn.charAt(0) == '[') {
                            throw SentenceArgumentTypeSyntaxException.build("Arrays aren't supported!");
                        }
                        throw SentenceArgumentTypeSyntaxException.build("you can only specify null for object returns!");
                    }
                }
            }
        }
        try {
            return new AssignedSentence(
                    clazz,
                    "*".equals(methodName),
                    Optional.ofNullable(this.cachedMethodInfo),
                    this.cachedClassRepresentation,
                    Optional.ofNullable(overriddenReturn)
            );
        } catch (Exception e) {
            throw SentenceArgumentTypeSyntaxException.build(e.getMessage());
        }
    }

    private Class<?> updateClassRepresentation() throws CommandSyntaxException {
        try {
            AtomicReference<ClassRepresentation> representation = new AtomicReference<>();
            Class<?> clazz = this.getClassFromCachedName();
            ClassExecutioner.tryRetransform(Sentence.ofGetter(buf -> representation.set(new ClassRepresentation(buf))), clazz);
            this.cachedClassRepresentation = representation.get();
            return clazz;
        } catch (Exception e) {
            throw SentenceArgumentTypeSyntaxException.build("Error retrieving class bytes");
        }
    }

    private Class<?> getClassFromCachedName() throws CommandSyntaxException {
        return Arrays.stream(ClassExecutioner.getLoadedClasses()).filter(c -> c.getName().equals(this.cachedClassName)).findFirst().orElseThrow(() -> {
            this.cachedClassRepresentation = null;
            this.cachedClassName = null;
            return SentenceArgumentTypeSyntaxException.build("Class not found!");
        });
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!this.containsMethod) {
            return SharedSuggestionProvider.suggest(
                    Arrays.stream(ClassExecutioner.getLoadedClasses()).map(Class::getName),
                    builder
            );
        } else {
            if (this.cachedClassRepresentation == null) {
                try {
                    this.updateClassRepresentation();
                } catch (CommandSyntaxException ignored) {}
            }
            if (this.isOverridingReturn) {
                String prefix = this.cachedClassName + "#" + this.cachedMethodName + "=";
                String returnType = this.cachedMethodInfo.getReturnType();
                if (returnType.equals("Z")) {
                    builder.suggest(prefix + "false").suggest(prefix + "true");
                } else if (returnType.equals("Ljava/lang/String;")) {
                    builder.suggest(prefix + "\"\"");
                } else if (returnType.startsWith("L")) {
                    builder.suggest(prefix + "null");
                }
                return builder.buildFuture();
            } else {
                return SharedSuggestionProvider.suggest(
                        this.cachedClassRepresentation.getMethodInfos().stream().map(mi -> this.cachedClassName + "#" + mi.getShortName()),
                        builder.suggest(this.cachedClassName + "#*")
                );
            }
        }
    }

    public static SentenceArgumentType sentence() {
        return new SentenceArgumentType();
    }

    public static AssignedSentence getSentence(final CommandContext<?> context, final String name) {
        return context.getArgument(name, AssignedSentence.class);
    }

    private static class SentenceArgumentTypeSyntaxException extends SimpleCommandExceptionType {

        public SentenceArgumentTypeSyntaxException(Message message) {
            super(message);
        }

        public static CommandSyntaxException build(String message) {
            return new CommandSyntaxException(new SentenceArgumentTypeSyntaxException(() -> message), () -> message);
        }
    }
}
