package gov.nist.registry.ws.serviceclasses;

import gov.nist.registry.common2.exception.MetadataException;
import gov.nist.registry.common2.exception.XdsValidationException;
import gov.nist.registry.common2.exception.XdsWSException;
import gov.nist.registry.common2.registry.Metadata;
import gov.nist.registry.common2.registry.MetadataSupport;
import gov.nist.registry.common2.registry.Response;
import gov.nist.registry.common2.registry.XdsCommon;
import gov.nist.registry.ws.AdhocQueryRequest;
import gov.nist.registry.ws.StoredQueryRequestSoapValidator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;

public class RegistryB  extends AbstractRegistry  {

	public boolean runContentValidationService(Metadata m, Response response) throws MetadataException {
		return true;
	}

	public String getServiceName() {
		return "R.b";
	}

	protected short getXdsVersion() {
		return XdsCommon.xds_b;
	}

	protected String getRTransactionName(OMElement ahqr) {
		return super.getRTransactionName(ahqr) + ".b";
	}

	protected void validateWS(boolean isSQ) throws XdsWSException {
		checkSOAP12();
		if (isAsync())
			throw new XdsWSException("Asynchronous web service request not acceptable on this endpoint" + 
					" - replyTo is " + getMessageContext().getReplyTo().getAddress());
	}

	protected void validateSubmitTransaction(OMElement sor)
	throws XdsValidationException {
		OMNamespace ns = sor.getNamespace();
		String ns_uri =  ns.getNamespaceURI();
		if (ns_uri == null || ! ns_uri.equals(MetadataSupport.ebLcm3.getNamespaceURI())) 
			throw new XdsValidationException("Invalid namespace on " + sor.getLocalName() + " (" + ns_uri + ")");

		String type = getRTransactionName(sor);

		if (!type.startsWith("SubmitObjectsRequest"))
			throw new XdsValidationException("Only SubmitObjectsRequest is acceptable on this endpoint, found " + sor.getLocalName());
	}

	protected void validateQueryTransaction(OMElement sor)
	throws XdsValidationException {
		OMNamespace ns = sor.getNamespace();
		String ns_uri =  ns.getNamespaceURI();
		if (ns_uri == null || ! ns_uri.equals(MetadataSupport.ebQns3.getNamespaceURI())) 
			throw new XdsValidationException("Invalid namespace on " + sor.getLocalName() + " (" + ns_uri + ")");

		String type = getRTransactionName(sor);

		if (!this.isSQ(sor))
			throw new XdsValidationException("Only StoredQuery is acceptable on this endpoint");

		new StoredQueryRequestSoapValidator(getXdsVersion(), getMessageContext()).runWithException();

	}

	protected void validateQueryInputDecoration(OMElement sor, AdhocQueryRequest a)
	throws XdsValidationException {

	}

	protected void decorateQueryOutput(OMElement sor, AdhocQueryRequest a, OMElement result) throws XdsValidationException {
		String home = getHomeParameter(sor, a);
		if (home != null && !home.equals(""))
			new RG().setHomeOnSQResponse(result, home);
	}

	protected String getHomeParameter(OMElement sor, AdhocQueryRequest a)
	throws XdsValidationException {
		String home;
		try {
			home = a.getHome(sor);
		} catch (Exception e) {
			throw new XdsValidationException("Error retrieving homeCommunityId from request", e);
		}
		return home;
	}

}