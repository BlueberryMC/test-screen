package net.blueberrymc.screenTester;

import java.io.FileDescriptor;
import java.security.Permission;

public class SecurityManagerExitTrap extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {}

    @Override
    public void checkPermission(Permission perm, Object context) {}

    @Override
    public void checkDelete(String file) {}

    @Override
    public void checkExec(String cmd) {}

    @Override
    public void checkRead(String file) {}

    @Override
    public void checkRead(FileDescriptor fd) {}

    @Override
    public void checkRead(String file, Object context) {}

    @Override
    public void checkWrite(String file) {}

    @Override
    public void checkWrite(FileDescriptor fd) {}

    @Override
    public void checkExit(int status) {
        super.checkExit(status);
        throw new Utils.ExitTrappedException();
    }
}
