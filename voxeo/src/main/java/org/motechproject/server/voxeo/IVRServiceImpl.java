/**
 * MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT
 *
 * Copyright (c) 2011 Grameen Foundation USA.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Grameen Foundation USA, nor its respective contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA AND ITS CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION USA OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package org.motechproject.server.voxeo;

import org.motechproject.server.service.ivr.CallDetailRecord;
import org.motechproject.server.service.ivr.CallInitiationException;
import org.motechproject.server.service.ivr.CallRequest;
import org.motechproject.server.service.ivr.IVRService;
import org.motechproject.server.voxeo.dao.AllPhoneCalls;
import org.motechproject.server.voxeo.domain.PhoneCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Voxeo specific implementation of the IVR Service interface
 *
 * Date: 07/03/11
 *
 */
public class IVRServiceImpl implements IVRService
{
    @Autowired
    private AllPhoneCalls allPhoneCalls;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public String tokenId;

    @Override
    public void initiateCall(CallRequest callRequest)
    {
        if (callRequest == null) {
            throw new IllegalArgumentException("CallRequest can not be null");
        }

        // Create a call record to track this call
        PhoneCall phoneCall = new PhoneCall(callRequest);
        phoneCall.setDirection(PhoneCall.Direction.OUTGOING);
        phoneCall.setDisposition(CallDetailRecord.Disposition.UNKNOWN);
        phoneCall.setStartDate(new Date());
        allPhoneCalls.add(phoneCall);

        String voxeoURL = "http://api.voxeo.net/SessionControl/CCXML10.start";
        //String tokenid = "tokenid=b418dfa2e50c744a8e631ea7edcc2050113f414661e4f700c0b0772381b163f0111974aa74ef9438ce6544fb";
        String phonenum = "phonenum=" + callRequest.getPhone();
        String vxmlURL =  "vxml=" + URLEncoder.encode(callRequest.getVxmlUrl());
        String externalId = "externalId=" + phoneCall.getId();

        if (0 != callRequest.getTimeOut()) {
            voxeoURL += "?tokenid=" + tokenId + "&" + phonenum + "&" + vxmlURL + "&" + externalId + "&timeout=" + callRequest.getTimeOut();
        } else {
            voxeoURL += "?tokenid=" + tokenId + "&" + phonenum + "&" + vxmlURL + "&" + externalId;
        }

        log.info("Initiating call to: " + callRequest.getPhone() + " VXML URL: " + callRequest.getVxmlUrl());
        log.info("Voxeo URL: " + voxeoURL);

        try {
            String line;
            String result = "";

            URL url = new URL(voxeoURL);

    		BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));

			while ((line = br.readLine()) != null)
				result += line;

            if ("success" != result) {
                log.error("Voxeo result: " + result);
                throw new CallInitiationException("Could not initiate call: non-success return from Voxeo");
            }
		} catch (MalformedURLException e) {
			log.error("MalformedURLException: ", e);
		} catch (Exception e) {
            log.error("Exception: ", e);
        }
    }
}