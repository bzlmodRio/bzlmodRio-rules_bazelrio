package org.bazelrio.deploy;


import net.schmizz.sshj.xfer.FileSystemFile;

public class DryRunDeployer extends BaseDeployer
{
    protected DryRunDeployer(boolean verbose) {
        super(verbose);
    }
    
    public void runCommand(String command) {
        System.out.println("Running command: " + command);
    }

    public void copyFile(FileSystemFile source, String destination) {
        // System.out.println("Copying '" + source + " to '" + destination + "'");
        System.out.println("Copying " + destination + "'");
    }
    
    protected boolean attemptConnection(String address) {
        System.out.println("Attempting to connect to " + address);
        return true;
    }
}