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
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class ClassArgumentType implements ArgumentType<Class<?>> {

    @Override
    public Class<?> parse(StringReader reader) throws CommandSyntaxException {
        String[] strings = reader.getRemaining().split(" ");
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        };
        String className = strings[0];
        try {
            return Arrays.stream(ClassExecutioner.getLoadedClasses()).filter(c -> c.getName().equals(className)).findFirst().orElseThrow();
        } catch (Exception e) {
            throw new CommandSyntaxException(new ClassArgumentTypeSyntaxException(() -> "Class not found"), () -> "Class not found");
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                Arrays.stream(ClassExecutioner.getLoadedClasses()).map(Class::getName),
                builder
        );
    }

    public static ClassArgumentType clazz() {
        return new ClassArgumentType();
    }

    public static Class<?> getClass(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Class.class);
    }

    private static class ClassArgumentTypeSyntaxException extends SimpleCommandExceptionType {

        public ClassArgumentTypeSyntaxException(Message message) {
            super(message);
        }
    }
}
