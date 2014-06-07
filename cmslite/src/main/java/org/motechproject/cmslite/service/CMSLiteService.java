package org.motechproject.cmslite.service;
/**
 * \defgroup cmslite CMS Lite
 * CMS Lite is lightweight content management supports multiple languages.
 */

import org.motechproject.cmslite.model.CMSLiteException;
import org.motechproject.cmslite.model.Content;
import org.motechproject.cmslite.model.ContentNotFoundException;
import org.motechproject.cmslite.model.StreamContent;
import org.motechproject.cmslite.model.StringContent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * \ingroup cmslite
 * CMS Lite is lightweight content management based on couchdb storage. It supports storing and retrieving of stream / text along with
 * custom meta data for each language
 */
@Component
public interface CMSLiteService {
    /**
     * Get Stream content for given language.
     *
     * @param language
     * @param name
     * @return StreamContent with checksum and data type
     * @throws ContentNotFoundException
     */
    StreamContent getStreamContent(String language, String name) throws ContentNotFoundException;

    /**
     * Get Text Content for given tag and language.
     *
     * @param language
     * @param name
     * @return
     * @throws ContentNotFoundException
     */
    StringContent getStringContent(String language, String name) throws ContentNotFoundException;

    /**
     * Remove stream content for given language.
     *
     * @param language
     * @param name
     * @throws ContentNotFoundException
     */
    void removeStreamContent(String language, String name) throws ContentNotFoundException;

    /**
     * Remove Text Content for given tag and language.
     *
     * @param language
     * @param name
     * @throws ContentNotFoundException
     */
    void removeStringContent(String language, String name) throws ContentNotFoundException;

    /**
     * Add content to CMS data-store
     *
     * @param content
     * @throws CMSLiteException
     * @see org.motechproject.cmslite.model.StreamContent
     * @see org.motechproject.cmslite.model.StringContent
     */
    void addContent(Content content) throws CMSLiteException;

    /**
     * Check if content available in stream format
     *
     * @param language
     * @param name
     * @return
     */
    boolean isStreamContentAvailable(String language, String name);

    /**
     * Check if content available in text format.
     *
     * @param language
     * @param name
     * @return
     */
    boolean isStringContentAvailable(String language, String name);

    List<Content> getAllContents();
    StringContent getStringContent(String stringContentId);
    StreamContent getStreamContent(String stringContentId);

    Byte[] retrieveStreamContentData(StreamContent instance);
}
