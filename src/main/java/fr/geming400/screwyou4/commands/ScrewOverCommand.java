package fr.geming400.screwyou4.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fr.geming400.screwyou4.entrypoints.ScrewYou4;
import fr.geming400.screwyou4.killer.ClassExecutioner;
import fr.geming400.screwyou4.killer.Sentence;
import fr.geming400.screwyou4.killer.parser.Infos;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.RandomSource;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class ScrewOverCommand {

    public static void initialize() {
        registerArguments();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, _, _) -> {
                    LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("screw")
                            .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR));
                    registerServerKillCommand(builder);
                    dispatcher.register(builder);
                }
        );

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommands.literal("screw-client");
            registerClientKillCommand(builder);

            dispatcher.register(builder);
        });
    }

    private static void registerArguments() {
        ArgumentTypeRegistry.registerArgumentType(
                ScrewYou4.id("class"),
                ClassArgumentType.class,
                SingletonArgumentInfo.contextFree(ClassArgumentType::new)
        );

        ArgumentTypeRegistry.registerArgumentType(
                ScrewYou4.id("method"),
                MethodArgumentType.class,
                SingletonArgumentInfo.contextFree(MethodArgumentType::new)
        );
    }

    public static void registerServerKillCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(
                Commands.literal("kill")
                        .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                .executes(ScrewOverCommand::serverKillRandomMethods))
                        .then(Commands.literal("class")
                                .then(Commands.argument("class_name", ClassArgumentType.clazz()).executes(ScrewOverCommand::serverKillSpecificClass)
                                        .then(Commands.argument("method_name", MethodArgumentType.method())
                                                .executes(ScrewOverCommand::serverKillSpecificMethod))
                                )
                        )
        );
    }

    public static void registerClientKillCommand(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        builder.then(
                ClientCommands.literal("kill")
                        .then(ClientCommands.argument("count", IntegerArgumentType.integer(0))
                                .executes(ScrewOverCommand::clientKillRandomFunctions))
                        .then(ClientCommands.literal("class")
                                .then(ClientCommands.argument("class_name", ClassArgumentType.clazz()).executes(ScrewOverCommand::clientKillSpecificClass)
                                        .then(ClientCommands.argument("method_name", MethodArgumentType.method())
                                                .executes(ScrewOverCommand::clientKllSpecificMethod))
                                )
                        )
        );
    }

    private static int killRandomFunctions(int n, RandomSource random) {
        for (int i = 0; i < n; i++)
            ClassExecutioner.kill(Sentence.ofIterating(random));

        return Command.SINGLE_SUCCESS;
    }

    private static int serverKillRandomMethods(CommandContext<CommandSourceStack> context) {
        int n = IntegerArgumentType.getInteger(context, "count");
        RandomSource random = Objects.requireNonNull(context.getSource().getPlayer()).getRandom();

        return killRandomFunctions(n, random);
    }



    private static int clientKillRandomFunctions(CommandContext<FabricClientCommandSource> context) {
        int n = IntegerArgumentType.getInteger(context, "count");
        RandomSource random = Objects.requireNonNull(context.getSource().getPlayer()).getRandom();

        return killRandomFunctions(n, random);
    }

    private static int killSpecificClass(CommandContext<?> context, RandomSource random, Consumer<Component> onFailure) {
        Class<?> clazz = ClassArgumentType.getClass(context, "class_name");
        Optional<Exception> exception = ClassExecutioner.tryRetransform(new Sentence(random, false), clazz);

        exception.ifPresent(e -> {
            Component message = Component.literal("Error while killing " + clazz.getSimpleName() + ", " + e.getClass().getSimpleName() + " : " + e.getMessage());
            onFailure.accept(message);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int serverKillSpecificClass(CommandContext<CommandSourceStack> context) {
        RandomSource random = Objects.requireNonNull(context.getSource().getPlayer()).getRandom().fork();
        return killSpecificClass(context, random, message -> context.getSource().sendFailure(message));
    }

    private static int clientKillSpecificClass(CommandContext<FabricClientCommandSource> context) {
        RandomSource random = Objects.requireNonNull(context.getSource().getPlayer()).getRandom().fork();
        return killSpecificClass(context, random, message -> context.getSource().sendError(message));
    }



    private static int killSpecificMethod(CommandContext<?> context, RandomSource random, Consumer<Component> onFailure) {
        Class<?> clazz = ClassArgumentType.getClass(context, "class_name");
        Infos.MethodInfo method = MethodArgumentType.getMethod(context, "method_name");

        Optional<Exception> ex = ClassExecutioner.tryRetransform(new Sentence(
                random,
                false,
                Optional.of(method),
                Optional.of(method.constantPool.classRepresentation),
                Optional.empty(),
                Optional.empty()
        ), clazz);

        ex.ifPresent(e -> {
            Component message = Component.literal("Error while killing " + clazz.getSimpleName() + ", " + e.getClass().getSimpleName() + " : " + e.getMessage());
            onFailure.accept(message);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int serverKillSpecificMethod(CommandContext<CommandSourceStack> context) {
        RandomSource random = Objects.requireNonNull(context.getSource().getPlayer()).getRandom().fork();
        return killSpecificMethod(context, random, message -> context.getSource().sendFailure(message));
    }

    private static int clientKllSpecificMethod(CommandContext<FabricClientCommandSource> context) {
        RandomSource random = Objects.requireNonNull(context.getSource().getPlayer()).getRandom().fork();
        return killSpecificMethod(context, random, message -> context.getSource().sendError(message));
    }
}
