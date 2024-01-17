package youyihj.probezs.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author youyihj
 */
public interface BracketHandlerService extends Remote {
    @Nullable
    String getLocalizedName(String expr) throws RemoteException;

    @Nullable
    String getIcon(String expr) throws RemoteException;

    @Nonnull
    String getTypeName(String expr) throws RemoteException;
}
