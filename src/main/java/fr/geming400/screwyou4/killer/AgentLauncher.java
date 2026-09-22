package fr.geming400.screwyou4.killer;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.IOException;

public class AgentLauncher {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("PID not found in arguments !");
        } else {
            VirtualMachine vm = null;
            try {
                File agentJar = new File(args[1]);
                System.out.printf("Attaching agent at %s...\n", agentJar.getAbsolutePath());
                vm = VirtualMachine.attach(args[0]);
                vm.loadAgent(args[1]);
            } catch (AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException e) {
                System.out.println(e.getMessage());
            } finally {
                if (vm != null) {
                    vm.detach();
                }
            }
            System.out.println("Attachment successful!");
        }
    }
}
