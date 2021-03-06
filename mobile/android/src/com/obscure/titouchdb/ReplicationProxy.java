package com.obscure.titouchdb;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.replicator.Replication.ChangeEvent;
import com.couchbase.lite.replicator.Replication.ChangeListener;

@Kroll.proxy(parentModule = TitouchdbModule.class)
public class ReplicationProxy extends KrollProxy implements ChangeListener {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final String   LCAT               = "ReplicationProxy";

    private AuthenticatorProxy    authenticatorProxy;

    private DatabaseProxy         databaseProxy;

    private KrollDict             lastError          = null;

    private Replication           replicator;

    public ReplicationProxy(DatabaseProxy databaseProxy, Replication replicator) {
        assert databaseProxy != null;
        assert replicator != null;
        this.databaseProxy = databaseProxy;
        this.replicator = replicator;

        replicator.addChangeListener(this);
    }

    @Override
    public void changed(ChangeEvent e) {
        KrollDict params = new KrollDict();
        params.put("source", this);
        params.put("status", replicator.getStatus().ordinal());

        fireEvent("change", params);
    }
    
    @Kroll.getProperty(name="authenticator")
    public AuthenticatorProxy getAuthenticator() {
        return authenticatorProxy;
    }
    
    @Kroll.getProperty(name = "changesCount")
    public int getChangesCount() {
        return replicator.getChangesCount();
    }

    @Kroll.getProperty(name = "completedChangesCount")
    public int getCompletedChangesCount() {
        return replicator.getCompletedChangesCount();
    }

    @Kroll.getProperty(name = "docIds")
    public String[] getDocIds() {
        // TODO fix in 1.0.4+ ?
        // https://github.com/couchbase/couchbase-lite-java-core/issues/307
        List<String> docids = null; // replicator.getDocIds();
        return docids != null ? docids.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY;
    }

    @Kroll.getProperty(name = "lastError")
    public KrollDict getError() {
        return lastError;
    }

    @Kroll.getProperty(name = "filter")
    public String getFilter() {
        return replicator.getFilter();
    }

    @Kroll.getProperty(name = "filterParams")
    public KrollDict getFilterParams() {
        return TypePreprocessor.toKrollDict(replicator.getFilterParams());
    }

    @Kroll.getProperty(name = "headers")
    public KrollDict getHeaders() {
        return TypePreprocessor.toKrollDict(replicator.getHeaders());
    }

    @Kroll.getProperty(name = "localDatabase")
    public DatabaseProxy getLocalDatabase() {
        return databaseProxy;
    }

    @Kroll.getProperty(name = "remoteUrl")
    public String getRemoteUrl() {
        URL url = replicator.getRemoteUrl();
        return url != null ? url.toString() : null;
    }

    @Kroll.getProperty(name = "status")
    public int getStatus() {
        return replicator.getStatus().ordinal();
    }

    @Kroll.getProperty(name = "continuous")
    public boolean isContinuous() {
        return replicator.isContinuous();
    }

    @Kroll.getProperty(name = "createTarget")
    public boolean isCreateTarget() {
        return replicator.shouldCreateTarget();
    }

    @Kroll.getProperty(name = "isPull")
    public boolean isPull() {
        return replicator.isPull();
    }

    @Kroll.getProperty(name = "isRunning")
    public boolean isRunning() {
        return replicator.isRunning();
    }

    @Kroll.method(runOnUiThread = true)
    public void restart() {
        replicator.restart();
    }

    @Kroll.setProperty(name="authenticator")
    public void setAuthenticator(AuthenticatorProxy authenticatorProxy) {
        this.authenticatorProxy = authenticatorProxy;
        replicator.setAuthenticator(authenticatorProxy != null ? authenticatorProxy.getAuthenticator() : null);
    }

    @Kroll.setProperty(name = "continuous")
    public void setContinuous(boolean continuous) {
        replicator.setContinuous(continuous);
    }

    @Kroll.setProperty(name = "createTarget")
    public void setCreateTarget(boolean createTarget) {
        replicator.setCreateTarget(createTarget);
    }

    @Kroll.method
    public void setCredential(@Kroll.argument(optional = true) KrollDict credential) {
        if (credential == null) {
            replicator.setAuthenticator(null);
        }
        else {
            // oddly enough, KrollDict.getString() crashes...
            String user = (String) credential.get("user");
            String pass = (String) credential.get("pass");
            Authenticator authenticator = AuthenticatorFactory.createBasicAuthenticator(user, pass);
            replicator.setAuthenticator(authenticator);
        }
    }

    @Kroll.setProperty(name = "docIds")
    public void setDocIds(String[] docids) {
        replicator.setDocIds(Arrays.asList(docids));
    }

    @Kroll.setProperty(name = "filter")
    public void setFilter(String filter) {
        replicator.setFilter(filter);
    }

    @Kroll.setProperty(name = "filterParams")
    public void setFilterParams(KrollDict filterParams) {
        replicator.setFilterParams(filterParams);
    }

    @Kroll.setProperty(name = "headers")
    public void setHeaders(KrollDict headers) {
        replicator.setHeaders(headers);
    }

    @Kroll.method(runOnUiThread = true)
    public void start() {
        replicator.start();
    }

    @Kroll.method(runOnUiThread = true)
    public void stop() {
        replicator.stop();
    }

}