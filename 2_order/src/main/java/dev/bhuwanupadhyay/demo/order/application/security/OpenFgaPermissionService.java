package dev.bhuwanupadhyay.demo.order.application.security;

import dev.bhuwanupadhyay.demo.order.model.OrderPermission;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteResponse;
import dev.openfga.sdk.errors.FgaError;
import dev.openfga.sdk.errors.FgaInvalidParameterException;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class OpenFgaPermissionService implements OrderPermission {

    private final OpenFgaClient fgaClient;

    public OpenFgaPermissionService(OpenFgaClient fgaClient) {
        this.fgaClient = fgaClient;
    }

    @Override
    public void assign(Permission permission) {
        try {
            ClientTupleKey tuple =
                    new ClientTupleKey() //
                            .user(permission.userAsCustomer()) //
                            .relation(permission.relationAsOwner()) //
                            ._object(permission.objectAsOrder());

            ClientWriteResponse response = fgaClient.writeTuples(List.of(tuple)).get();

            if (response.getStatusCode() >= 300) {
                throw new OpenFgaPermissionException(
                        String.format(
                                "%s: %s",
                                "Unable to write permission tuples", response.getRawResponse()));
            }

        } catch (FgaInvalidParameterException | InterruptedException | ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FgaError) {
                throw new OpenFgaPermissionException(
                        String.format(
                                "%s: %s", e.getMessage(), ((FgaError) cause).getResponseData()),
                        e);
            }
            throw new OpenFgaPermissionException(e.getMessage(), e);
        }
    }
}
