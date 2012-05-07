package org.motechproject.outbox.api.service;

import org.motechproject.model.MotechEvent;
import org.motechproject.outbox.api.contract.SortKey;
import org.motechproject.outbox.api.domain.OutboundVoiceMessage;
import org.motechproject.outbox.api.domain.OutboundVoiceMessageStatus;
import org.motechproject.outbox.api.domain.VoiceMessageType;

import java.util.List;

/**
 * \defgroup outbox Outbox
 */

/**
 * \ingroup outbox
 * <p></p>
 * Voice Outbox Service
 * <p></p>
 * Provides methods to get information and manage messages in the party voice outbox. Each party (Patient, Nurse,
 * Doctor, etc.) can have a voice outbox.
 */
public interface VoiceOutboxService {

    /**
     * Adds the given outbound voice message to the parties' voice outbox .A party is determined by external ID
     * in the given outbound voice message. All messages added will by default be in PENDING status and creationTime
     * will stamped with the current datetime.
     *
     * @param outboundVoiceMessage - outbound voice message to be added to the outbox
     */
    public void addMessage(OutboundVoiceMessage outboundVoiceMessage);

    /**
     * Retrieves the next message from the outbox of the party identified by the given ExternalID. Returns null if there
     * are no more pending messages in the outbox.
     * <p></p>
     * A next message is a message which is not expired with the given status that belongs to this party, the oldest
     * creation time and the highest priority among other pending messages in the external outbox.
     *
     * @param externalId    - unique identifier of the party
     * @param messageStatus - {@link OutboundVoiceMessageStatus} of the message
     * @return OutboundVoiceMessage - outbound voice message for the given external Id and status
     */
    public OutboundVoiceMessage getNextMessage(String externalId, OutboundVoiceMessageStatus messageStatus);

    /**
     * Retrieves the outbound voice message with the given MessageId stored in the outbox
     *
     * @param outboundVoiceMessageId - unique id of the message to be retrieved
     * @return OutboundVoiceMessage - outbound voice message for the given MessageId
     */
    public OutboundVoiceMessage getMessageById(String outboundVoiceMessageId);

    /**
     * Removes a message with the given MessageId from the outbox
     *
     * @param outboundVoiceMessageId - unique id of the message to be removed from outbox
     */
    public void removeMessage(String outboundVoiceMessageId);

    /**
     * Updates the outbox message found by the MessageId with the given status
     *
     * @param outboundVoiceMessageId - unique id of the message to be updated
     * @param status                 - {@link OutboundVoiceMessageStatus} with which the message has to be updated
     */
    public void setMessageStatus(String outboundVoiceMessageId, OutboundVoiceMessageStatus status);

    /**
     * Saves the message with given id in the outbox for period (number of days) specified in the outbox configuration.
     * <p></p>
     * By default sets the message status to <i>SAVED</i> and expiration date to <i>current date + number of days</i>
     * in the outbox configuration. {@link #setNumDaysKeepSavedMessages(int)}
     *
     * @param outboundVoiceMessageId - unique id of the message to be saved
     */
    public void saveMessage(String outboundVoiceMessageId);

    /**
     * Returns number of messages in the outbox of the party with the given ExternalId and {@link OutboundVoiceMessageStatus}
     *
     * @param externalId    - unique identifier of the party
     * @param messageStatus - {@link OutboundVoiceMessageStatus} of the messages to be counted
     * @return - count of messages found in outbox
     */
    public int getNumberOfMessages(String externalId, OutboundVoiceMessageStatus messageStatus);

    /**
     * Returns number of  messages in the outbox of the party with the given ExternalId, VoiceMessageTypeName and
     * {@link OutboundVoiceMessageStatus}
     *
     * @param externalId           - unique identifier of the party
     * @param messageStatus        - {@link OutboundVoiceMessageStatus} of the messages to be counted
     * @param voiceMessageTypeName - name of the {@link VoiceMessageType}
     * @return - count of messages found in outbox
     */
    public int getNumberOfMessages(String externalId, OutboundVoiceMessageStatus messageStatus, String voiceMessageTypeName);


    /**
     * Returns messages in the outbox of the party with the given ExternalId and {@link OutboundVoiceMessageStatus} sorted by the given {@link SortKey}
     *
     * @param externalId           - unique identifier of the party
     * @param status               - {@link OutboundVoiceMessageStatus} of the messages to be counted
     * @param sortKey              - sort key to be used to sort the filtered list of messages. See {@link SortKey}
     * @return - List of messages found in outbox
     */
    public List<OutboundVoiceMessage> getMessages(String externalId, OutboundVoiceMessageStatus status, SortKey sortKey);

    /**
     * Sets the number of days for which a message saved by the patient will be kept in outbox as SAVED message
     *
     * @param numDaysKeepSavedMessages - value representing number of days
     */
    public void setNumDaysKeepSavedMessages(int numDaysKeepSavedMessages);

    /**
     * Number of days for which a message saved by the patient will be kept in outbox as SAVED message
     *
     * @return - value representing number of days
     */
    public int getNumDaysKeepSavedMessages();

    /**
     * Sets the max number of pending messages after which the outbox will send an event {@link MotechEvent}
     *
     * @param maxNumberOfPendingMessages - a value representing the maximum number of pending messages
     */
    public void setMaxNumberOfPendingMessages(int maxNumberOfPendingMessages);

    /**
     * Maximum number of pending messages before an event is raised while adding messages {@link #addMessage(OutboundVoiceMessage)}
     *
     * @return - a value representing the maximum number of pending messages
     */
    public int getMaxNumberOfPendingMessages();

    /**
     * Retrieves the next pending message marking the lastMessage as PLAYED
     *
     * @param lastMessageId - the MessageId of the last message that has been PLAYED
     * @param externalId - unique identifier of the party
     * @return - the next pending message
     */
    public OutboundVoiceMessage nextMessage(String lastMessageId, String externalId);
}
