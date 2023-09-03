package org.bazelrio.deploy;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.scp.SCPUploadClient;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import java.io.IOException;
import java.security.PublicKey;
import java.util.List;
import java.util.ArrayList;


public abstract class SshDeployer extends BaseDeployer {

    private static class CommandFailedException extends IOException {
        public CommandFailedException(String message) { super(message); }
    }

    private static class NoopKeyVerifier implements HostKeyVerifier {
        public List<String> findExistingAlgorithms(String _hostname, int _port) {
            return new ArrayList<>();
        }

        public boolean verify(String _hostname, int _port, PublicKey _key) {
            return true;
        }
    }


    private final SSHClient client;
    private SCPUploadClient scp;

    protected SshDeployer(boolean verbose) throws IOException {
        super(verbose);

        client = new SSHClient();
        client.addHostKeyVerifier(new NoopKeyVerifier());
        client.useCompression();
    }


    @Override
    public void runCommand(String commandString) throws IOException {
        if (client == null) {
            throw new RuntimeException("Bad setup");
        }

        if (verbose) {
            System.out.println(String.format("Running %s", commandString));
        }
        Command command = client.startSession().exec(commandString);
        command.join();
        int exitStatus = command.getExitStatus();
        if (exitStatus != 0) {
            throw new CommandFailedException(String.format(
                "Command %s exited with code %d", commandString, exitStatus));
        }
    }

    @Override
    protected boolean attemptConnection(String address) {

      try {
        client.connect(address);
        client.authPassword("admin", "");
        scp = client.newSCPFileTransfer().newSCPUploadClient();

        return true;
      } catch (IOException e) {
        if (verbose) {
          System.err.println(
              String.format("Error connecting to %s: %s", address, e));
        }
      }
      return false;
    }
}
