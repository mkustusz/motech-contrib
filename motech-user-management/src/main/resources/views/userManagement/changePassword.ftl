<#macro changePassword>
<form class="modal hide form-horizontal" id="changePasswordModal" submitOnEnterKey="true" action="<@spring.url '/userManagement/changePassword'/>">
    <div class="modal-header">
        <button class="close" data-dismiss="modal">x</button>
        <h3>Change Password</h3>
    </div>
    <div class="modal-body">
        <div id="changePasswordServerSideError" class="alert alert-error hide"></div>
        <div id="changePasswordError" class="alert alert-error hide"></div>

        <div class="control-group">
            <label class="control-label" for="userName">User Name</label>

            <div class="controls">
                <input class="input-xlarge" type="hidden" name="userName" id="userName" value=""/>
                <span name="userNameSpan" id="userNameSpan"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="currentPassword">Current Password *</label>

            <div class="controls">
                <input class="input-xlarge" type="password" name='currentPassword' id="currentPassword"
                       autofocus="autofocus"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="newPassword">New Password *</label>

            <div class="controls">
                <input class="input-xlarge" type="password" name='newPassword' id="newPassword"/>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="confirmNewPassword">Confirm New Password *</label>

            <div class="controls">
                <input class="input-xlarge" type="password" name='confirmNewPassword' id="confirmNewPassword"/>
            </div>
        </div>
    </div>
    <div class="modal-footer">

        <button type="submit" class="btn  btn-primary" id="changePassword"><i class="icon-ok icon-white"></i> Save</button>
        <button class="btn " data-dismiss="modal"><i class="icon-remove"></i> Close</button>
    </div>
</form>
<script type="text/javascript" src="<@spring.url '/motech-user-management/js/changePassword.js'/>"></script>
</#macro>