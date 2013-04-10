package org.motechproject.couch.mrs.model;

import org.ektorp.support.TypeDiscriminator;
import org.motechproject.commons.couchdb.model.MotechBaseDataObject;
import org.motechproject.mrs.domain.MRSPerson;
import org.motechproject.mrs.domain.MRSProvider;

@TypeDiscriminator("doc.type === 'Provider'")
public class CouchProvider extends MotechBaseDataObject implements MRSProvider {

    private static final long serialVersionUID = 1L;
    private final String type = "Provider";

    private CouchPerson person;
    private String providerId;

    CouchProvider() { }

    public CouchProvider(String providerId, MRSPerson person) {
        super();
        this.setType(type);
        this.providerId = providerId;
        this.person = (CouchPerson) person;
    }

    public MRSPerson getPerson() {
        return person;
    }

    @Override
    public void setPerson(MRSPerson person) {
        this.person = (CouchPerson) person;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
