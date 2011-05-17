/* Copyright 2010 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.model.util;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowHandle;



/**
 * A utility class to support the FlowManager in its interactions
 * with a REST-based flow server a.k.a. Passerelle Manager instance.
 * 
 * @author erwin
 *
 */
public class RESTFacade {
	
	private final static Logger logger = LoggerFactory.getLogger(RESTFacade.class);
	
	private HttpClient httpClient;
	
	public RESTFacade(int connectionTimeout, int socketTimeout) {
		httpClient = new HttpClient();
		HttpClientParams params = new HttpClientParams();
		params.setSoTimeout(socketTimeout);
		params.setConnectionManagerTimeout(connectionTimeout);
		httpClient.setParams(params);
	}
	
	/**
	 * Get all flow handles from a Passerelle Manager server instance.
	 * The baseURL should identify the REST service, i.e. be of the form :
	 * <code>http://localhost:8080/PasserelleManagerService/V1.0</code>.
	 * 
	 * <br/>
	 * For "legacy" compatibility, also <code>http://localhost:8080/PasserelleManagerService/V1.0/jobs</code>
	 * is still supported, but discouraged.
	 * 
	 * @param baseURL should be not-null or an NPE will be thrown!
	 * @return
	 * @throws PasserelleException
	 */
	public Collection<FlowHandle> getAllRemoteFlowHandles(URL baseURL) throws PasserelleException {
		String baseURLStr = baseURL.toString();
		
		if(!baseURLStr.endsWith("jobs")) {
			baseURLStr += "/jobs";
		}
		String jobHeadersResponse = invokeMethodForURL(new GetMethod(baseURLStr));
		if(jobHeadersResponse!=null) {
			Collection<FlowHandle> flowHandles = buildFlowHandles(jobHeadersResponse);
			return flowHandles;
		} else {
			return new ArrayList<FlowHandle>(0);
		}
	}
	
	/**
	 * Get all flow handles for all currently executing jobs from a Passerelle Manager server instance.
	 * The baseURL should identify the REST service, i.e. be of the form :
	 * <code>http://localhost:8080/PasserelleManagerService/V1.0</code>.
	 * 
	 * @param baseURL
	 * @return
	 * @throws PasserelleException
	 */
	public Collection<FlowHandle> getAllRemoteExecutingFlowHandles(URL baseURL) throws PasserelleException {
		String baseURLStr = baseURL.toString();
		baseURLStr += "/scheduledjobs";
		String scheduledJobsResponse = invokeMethodForURL(new GetMethod(baseURLStr));
		if(scheduledJobsResponse!=null) {
			Collection<FlowHandle> flowHandles = buildExecutingFlowHandles(baseURL, scheduledJobsResponse);
			return flowHandles;
		} else {
			return new ArrayList<FlowHandle>(0);
		}
	}
	
	public Flow startFlowRemotely(Flow flow) throws PasserelleException, IllegalStateException, IllegalArgumentException {
		FlowHandle handle = startFlowRemotely(flow.getHandle());
		return flow;
	}
	
	/**
	 * 
	 * @param fHandle
	 * @return the flow handle with its newly obtained execId
	 * 
	 * @throws PasserelleException
	 * @throws IllegalStateException e.g. when the flow is already executing
	 * @throws IllegalArgumentException e.g. when the handle does not correspond to an existing flow
	 * 
	 */
	public FlowHandle startFlowRemotely(FlowHandle fHandle) throws PasserelleException, IllegalStateException, IllegalArgumentException {
		String startURL = fHandle.getAuthorativeResourceLocation().toString();
		startURL += "/launch";
		String startInfo = invokeMethodForURL(new PostMethod(startURL));
		SAXBuilder parser = new SAXBuilder();
		Document doc  = null;
		try {
			doc = parser.build(new StringReader(startInfo));
		} catch (Exception e) {
			throw new PasserelleException("Unable to parse response data",startInfo,e);
		}
		if( doc != null ) {
			Element scheduledJobElement = doc.getRootElement().getChild("scheduledJob");
			Element execInfo = scheduledJobElement.getChild("execInfo");
			String execId = execInfo.getAttributeValue("id");
			String execHREF = execInfo.getAttributeValue("href");
			fHandle.setExecId(execId);
			try {
				fHandle.setExecResourceLocation(new URL(execHREF));
			} catch (Exception e) {
				throw new PasserelleException("Invalid URL "+execHREF+" in response ",startInfo,e);
			}
		}
		return fHandle;
	}

	/**
	 * 
	 * @param fHandle should have a valid execId, corresponding to an actual executing flow
	 * @return the flow handle without its execId
	 * @throws PasserelleException
	 * @throws IllegalStateException e.g. when the flow is not executing
	 * @throws IllegalArgumentException e.g. when the handle does not correspond to an existing flow
	 */
	public FlowHandle stopFlowRemotely(FlowHandle fHandle) throws PasserelleException, IllegalStateException, IllegalArgumentException {
		String stopURL = fHandle.getExecResourceLocation().toString()+"/stop";
		String stopInfo = invokeMethodForURL(new PostMethod(stopURL));
		fHandle.setExecId(null);
		fHandle.setExecResourceLocation(null);
		return fHandle;
	}

	public FlowHandle getRemoteFlowHandle(URL modelJobURL) throws PasserelleException {
		String jobDetailResponse = invokeMethodForURL(new GetMethod(modelJobURL.toString()));
		FlowHandle flowHandle = buildFlowHandle(modelJobURL, jobDetailResponse);
		return flowHandle;
	}

	private String invokeMethodForURL(HttpMethod method) {
		try {
			// Execute the method.
			int statusCode = httpClient.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				logger.warn("Response status error : " + method.getStatusLine());
			}
			
			String response = method.getResponseBodyAsString();
			if(logger.isDebugEnabled()) {
				logger.debug("Received response\n" + response);
			}
			
			return response;
		} catch (HttpException e) {
			logger.error("Fatal protocol violation: ",e);
			return null;
		} catch (IOException e) {
			logger.error("Fatal transport error: ",e);
			return null;
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<FlowHandle> buildFlowHandles(String jobHeadersResponse) throws PasserelleException {
		Collection<FlowHandle> flowHandles = new ArrayList<FlowHandle>();
		SAXBuilder parser = new SAXBuilder();
		Document doc  = null;
		try {
			doc = parser.build(new StringReader(jobHeadersResponse));
		} catch (Exception e) {
			throw new PasserelleException("Unable to parse response data",jobHeadersResponse,e);
		}
		if( doc != null ) {
			List<Element> jobElements = doc.getRootElement().getChildren("job");
			for (Element jobElement : jobElements) {
				String id = jobElement.getAttributeValue("id");
				String href = jobElement.getAttributeValue("href");
				String name = jobElement.getAttributeValue("name");
				try {
					FlowHandle flowHandle = new FlowHandle(new Long(id), name, new URL(href));
					flowHandles.add(flowHandle);
				} catch (Exception e) {
					throw new PasserelleException("Invalid URL "+href+" in response ",jobHeadersResponse,e);
				}
			}
		}
		return flowHandles;
	}

	@SuppressWarnings("unchecked")
	private Collection<FlowHandle> buildExecutingFlowHandles(URL baseURL, String jobHeadersResponse) throws PasserelleException {
		Collection<FlowHandle> flowHandles = new ArrayList<FlowHandle>();
		SAXBuilder parser = new SAXBuilder();
		Document doc  = null;
		try {
			doc = parser.build(new StringReader(jobHeadersResponse));
		} catch (Exception e) {
			throw new PasserelleException("Unable to parse response data",jobHeadersResponse,e);
		}
		if( doc != null ) {
			List<Element> scheduledJobElements = doc.getRootElement().getChildren("scheduledJob");
			for (Element scheduledJobElement : scheduledJobElements) {
				Element execInfo = scheduledJobElement.getChild("execInfo");
				String execId = execInfo.getAttributeValue("id");
//				String execName = execInfo.getAttributeValue("name");
				String execHREF = execInfo.getAttributeValue("href");
				Element jobElement = scheduledJobElement.getChild("job");
				String jobId = jobElement.getAttributeValue("id");
				String jobHREF = jobElement.getAttributeValue("href");
				String jobName = jobElement.getAttributeValue("name");
				try {
					FlowHandle flowHandle = new FlowHandle(new Long(jobId), jobName, new URL(jobHREF));
					flowHandle.setExecId(execId);
					flowHandle.setExecResourceLocation(new URL(execHREF));
					flowHandles.add(flowHandle);
				} catch (Exception e) {
					throw new PasserelleException("Invalid URL "+jobHREF+" in response ",jobHeadersResponse,e);
				}
			}
		}
		return flowHandles;
	}


	protected FlowHandle buildFlowHandle(URL modelJobURL, String jobDetail) throws PasserelleException {
		FlowHandle flowHandle=null;
		SAXBuilder parser = new SAXBuilder();
		Document doc  = null;
		try {
			doc = parser.build(new StringReader(jobDetail));
		} catch (Exception e) {
			throw new PasserelleException("Unable to parse response data",jobDetail,e);
		}
		if( doc != null ) {
			String id = doc.getRootElement().getChildText("id");
			String name = doc.getRootElement().getChildText("name");
//			String modelName = doc.getRootElement().getChildText("model");
			String moml = doc.getRootElement().getChildText("modelMoml");
			flowHandle = new FlowHandle(new Long(id),name,modelJobURL);
			flowHandle.setMoml(moml);
		}
		
		return flowHandle;
	}
}
