package fr.geming400.screwyou4.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.geming400.screwyou4.killer.ClassExecutioner;
import fr.geming400.screwyou4.killer.Sentence;
import fr.geming400.screwyou4.killer.parser.ClassRepresentation;
import fr.geming400.screwyou4.killer.parser.Infos;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class MethodArgumentType implements ArgumentType<Infos.MethodInfo> {

    private String className;
    private ClassRepresentation classRepresentation;

    @Override
    public Infos.MethodInfo parse(StringReader reader) throws CommandSyntaxException {
        String[] args = reader.getString().split(" ");
        final String classArgument;
        String s = null;
        int index = 1;
        for (String arg : args) {
            if (index + arg.length() >= reader.getCursor()) {
                s = arg;
                break;
            }
            index += arg.length() + 1;
        }
        if (s == null) {
            throw MethodArgumentTypeSyntaxException.create("Class not found");
        }
        classArgument = s;
        String[] strings = reader.getRemaining().split(" ");
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }
        String methodName = strings[0];
        if (classArgument.equals(this.className)) {
            return this.classRepresentation.getMethodInfos().stream().filter(info -> info.getShortName().equals(methodName)).findFirst().orElseThrow(() -> MethodArgumentTypeSyntaxException.create("Method not found"));
        }
        Class<?> clazz = Arrays.stream(ClassExecutioner.getLoadedClasses()).filter(c -> c.getName().equals(classArgument)).findFirst().orElseThrow(() -> MethodArgumentTypeSyntaxException.create("Class not found"));
        this.className = classArgument;
        return this.getMethodInfos(clazz).stream().filter(info -> info.getShortName().equals(methodName)).findFirst().orElseThrow(() -> MethodArgumentTypeSyntaxException.create("Method not found"));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        try {
            String command = context.getInput();
            if (command.endsWith(" ")) {
                command += "_";
            }
            String[] strings = command.split(" ");
            Class<?> clazz = Arrays.stream(ClassExecutioner.getLoadedClasses()).filter(c -> c.getName().equals(strings[strings.length - 2])).findFirst().orElseThrow(() -> MethodArgumentTypeSyntaxException.create("Class not found"));
            if (strings[strings.length - 2].equals(this.className)) {
                return SharedSuggestionProvider.suggest(
                        this.classRepresentation.getMethodInfos().stream().map(Infos.MethodInfo::getShortName).toList(),
                        builder
                );
            }
            return SharedSuggestionProvider.suggest(
                    this.getMethodInfos(clazz).stream().map(Infos.MethodInfo::getShortName).toList(),
                    builder
            );
        } catch (Exception e) {
            return Suggestions.empty();
        }
    }

    private ArrayList<Infos.MethodInfo> getMethodInfos(Class<?> clazz) throws CommandSyntaxException {
        try {
            AtomicReference<ClassRepresentation> representation = new AtomicReference<>();
            ClassExecutioner.tryRetransform(Sentence.ofGetter(buf -> representation.set(new ClassRepresentation(buf))), clazz);
            this.classRepresentation = representation.get();
            return representation.get().getMethodInfos();
        } catch (Exception e) {
            throw MethodArgumentTypeSyntaxException.create("Error retrieving class bytes");
        }
    }

    public static MethodArgumentType method() {
        return new MethodArgumentType();
    }

    public static Infos.MethodInfo getMethod(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Infos.MethodInfo.class);
    }

    private static class MethodArgumentTypeSyntaxException extends SimpleCommandExceptionType {

        public MethodArgumentTypeSyntaxException(Message message) {
            super(message);
        }

        public static CommandSyntaxException create(String message) {
            return new CommandSyntaxException(new MethodArgumentTypeSyntaxException(() -> message), () -> message);
        }
    }
}
