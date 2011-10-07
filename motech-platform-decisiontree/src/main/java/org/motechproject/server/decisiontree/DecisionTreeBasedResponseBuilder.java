package org.motechproject.server.decisiontree;

import org.motechproject.decisiontree.model.*;
import org.motechproject.server.service.ivr.IVRContext;
import org.motechproject.server.service.ivr.IVRResponseBuilder;
import org.motechproject.server.service.ivr.PostTreeCallContinuation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DecisionTreeBasedResponseBuilder {
    PostTreeCallContinuation postTreeCallContinuation;

	@Autowired
	public DecisionTreeBasedResponseBuilder(PostTreeCallContinuation postTreeCallContinuation) {
        this.postTreeCallContinuation = postTreeCallContinuation;
	}
	
    public IVRResponseBuilder ivrResponse(Node node, IVRContext ivrContext, IVRResponseBuilder ivrResponseBuilder, boolean playOnlyQuestionPrompts) {
        List<Prompt> prompts = node.getPrompts();
        boolean hasTransitions = node.hasTransitions();
        for (Prompt prompt : prompts) {
            if (isQuestionPrompt(prompt) || !playOnlyQuestionPrompts) {
                ITreeCommand command = prompt.getCommand();
                boolean isAudioPrompt = prompt instanceof AudioPrompt;
                if (command == null) {
                    buildPrompts(ivrResponseBuilder, prompt.getName(), isAudioPrompt);
                } else {
                    String[] promptsFromCommand = command.execute(ivrContext);
                    for (String promptFromCommand : promptsFromCommand) {
                        buildPrompts(ivrResponseBuilder, promptFromCommand, isAudioPrompt);
                    }
                }
            }
        }
        if (hasTransitions && !hasUrlTransition(node)) {
            ivrResponseBuilder.collectDtmf(maxLenOfTransitionOptions(node));
        } else {
            postTreeCallContinuation.continueCall(ivrContext, ivrResponseBuilder);
        } 
        if (hasUrlTransition(node)){
        	Map<String, Transition> transitions = node.getTransitions();
        	Transition transition = transitions.get(transitions.keySet().iterator().next());
        	if (transition instanceof URLTransition) {
        		URLTransition urlTransition = (URLTransition)transition;
        		ivrResponseBuilder.withNextUrl(urlTransition.getUrl());
        	}
        }
        return ivrResponseBuilder;
    }

    private boolean hasUrlTransition(Node node) {
        if (node.getTransitions().size() == 1){
            Map<String, Transition> transitions = node.getTransitions();
            Transition transition = transitions.get(transitions.keySet().iterator().next());
            if (transition instanceof URLTransition)
                return true;
        }
        return false;
    }

    private boolean isQuestionPrompt(Prompt prompt) {
        return (prompt instanceof MenuAudioPrompt) || !(prompt instanceof AudioPrompt);
    }

    private int maxLenOfTransitionOptions(Node node) {
    	int maxLen = 0;
		for (String key :node.getTransitions().keySet()){
			if (maxLen<key.length())maxLen = key.length();
		}
		return maxLen;
	}

	private void buildPrompts(IVRResponseBuilder ivrResponseBuilder, String promptName, boolean isAudioPrompt) {
        if (isAudioPrompt) ivrResponseBuilder.withPlayAudios(promptName);
        else ivrResponseBuilder.withPlayTexts(promptName);
    }
}
