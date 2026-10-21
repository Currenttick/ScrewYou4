package fr.geming400.screwyou4.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.geming400.screwyou4.entrypoints.ScrewYou4;
import fr.geming400.screwyou4.killer.ClassExecutioner;
import fr.geming400.screwyou4.killer.Sentence;
import fr.geming400.screwyou4.killer.parser.Infos;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;

public class ScrewOverCommand {

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, _, _) -> {
                    LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("screw");
                    registerKillCommand(builder);
                    dispatcher.register(builder);
                }
        );
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

    public static void registerKillCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(
                Commands.literal("kill")
                        .then(Commands.argument("count", IntegerArgumentType.integer(0)).executes(context -> {
                            int nb = IntegerArgumentType.getInteger(context, "count");
                            for (int i = 0; i < nb; i++) {
                                ClassExecutioner.kill(Sentence.ofIterating(Objects.requireNonNull(context.getSource().getPlayer()).getRandom()));
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("class")
                                .then(Commands.argument("class_name", ClassArgumentType.clazz()).executes(context -> {
                                    Class<?> clazz = ClassArgumentType.getClass(context, "class_name");
                                    Optional<Exception> ex = ClassExecutioner.tryRetransform(new Sentence(Objects.requireNonNull(context.getSource().getPlayer()).getRandom().fork(), false), clazz);
                                    ex.ifPresent(e -> context.getSource().sendFailure(Component.literal("Error while killing " + clazz.getSimpleName() + ", " + e.getClass().getSimpleName() + " : " + e.getMessage())));
                                    return Command.SINGLE_SUCCESS;
                                })
                                        .then(Commands.argument("method_name", MethodArgumentType.method()).executes(context -> {
                                            Class<?> clazz = ClassArgumentType.getClass(context, "class_name");
                                            Infos.MethodInfo method = MethodArgumentType.getMethod(context, "method_name");
                                            Optional<Exception> ex = ClassExecutioner.tryRetransform(new Sentence(
                                                    Objects.requireNonNull(context.getSource().getPlayer()).getRandom().fork(),
                                                    false,
                                                    Optional.of(method),
                                                    Optional.of(method.constantPool.classRepresentation),
                                                    Optional.empty(),
                                                    Optional.empty()
                                            ), clazz);
                                            ex.ifPresent(e -> context.getSource().sendFailure(Component.literal("Error while killing " + clazz.getSimpleName() + ", " + e.getClass().getSimpleName() + " : " + e.getMessage())));
                                            return Command.SINGLE_SUCCESS;
                                                }))))
        );
    }

}
