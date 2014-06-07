package org.motechproject.cmslite.web;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.motechproject.cmslite.model.Content;
import org.motechproject.cmslite.model.StreamContent;
import org.motechproject.cmslite.model.StringContent;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

public final class ResourceFilter {

    private ResourceFilter() {
    }

    public static List<ResourceDto> filter(GridSettings settings, final List<Content> contents) {
        List<ResourceDto> resourceDtos = new ArrayList<>();

        for (final Content content : contents) {
            if (settings.isCorrect(content.getName(), content.getLanguage(), getContentType(content))) {
                ResourceDto dto = (ResourceDto) CollectionUtils.find(resourceDtos, new Predicate() {
                    @Override
                    public boolean evaluate(Object object) {
                        return object instanceof ResourceDto &&
                                equalsContent((ResourceDto) object, content.getName(), getContentType(content));
                    }
                });

                if (dto == null) {
                    resourceDtos.add(new ResourceDto(content));
                } else {
                    dto.addLanguage(content.getLanguage());
                }
            }
        }

        return resourceDtos;
    }

    public static String getContentType(Content content) {
        String contentType;

        if (content instanceof StringContent) {
            contentType = "string";
        } else if (content instanceof StreamContent) {
            contentType = "stream";
        } else {
            contentType = "";
        }

        return contentType;
    }

    public static boolean equalsContent(ResourceDto dto, String contentName, String contentType) {
        return dto.getName().equals(contentName) && equalsIgnoreCase(dto.getType(), contentType);
    }
}
