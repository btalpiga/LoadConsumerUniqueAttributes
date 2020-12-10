package com.nyble.api.messages;

public class ConsumerInfoResponse {

    public ConsumerInfo[] consumers;
    public transient boolean hasError;
    public transient String raw;

    public static class ConsumerInfo {
        private String firstname;
        private String lastname;
        private String birthdate;
        private String email;
        private String phone;
        private String city;
        private String county;
        private String street_address;
        private String error;
        private String error_message;
        private String sku_1;
        private String sku;

        //flags
        private Boolean opt_in;
        private Boolean opt_in_sms;
        private Boolean opt_in_email;
        private Boolean opt_in_market_analysis;
        private Boolean opt_in_postaladdress;
        private Boolean is_phone_invalid;
        private Boolean sms_confirmed;
        private Boolean is_email_invalid;
        private Boolean email_confirmed;
        private Boolean is_postal_address_invalid;
        private Boolean is_web_user;
        private Boolean web_account_banned;
        private Integer status;

        public Boolean getGdprApproval(){
            return opt_in_market_analysis!=null;
        }

        public Boolean getOptIn() {
            if(opt_in == null) opt_in = false;
            return opt_in;
        }

        public Boolean getOptInSms() {
            if(opt_in_sms == null) opt_in_sms = false;
            return opt_in_sms;
        }

        public Boolean getOptInEmail() {
            if(opt_in_email == null) opt_in_email = false;
            return opt_in_email;
        }

        public Boolean getOptInMarketAnalysis() {
            if(opt_in_market_analysis == null) return false;
            return opt_in_market_analysis;
        }

        public Boolean getOptInPostalAddress() {
            if(opt_in_postaladdress == null) opt_in_postaladdress = false;
            return opt_in_postaladdress;
        }

        public Boolean isPhoneValid() {
            if(is_phone_invalid == null) is_phone_invalid = false;
            return !is_phone_invalid && phone!=null;
        }

        public Boolean smsConfirmed() {
            if(sms_confirmed == null) sms_confirmed = false;
            return sms_confirmed && phone != null;
        }

        public Boolean isEmailValid() {
            if(is_email_invalid == null) is_email_invalid = false;
            return !is_email_invalid && email != null;
        }

        public Boolean emailConfirmed() {
            if(email_confirmed == null) email_confirmed = false;
            return email_confirmed && email != null;
        }

        public Boolean isPostalAddressValid() {
            if(is_postal_address_invalid == null) is_postal_address_invalid = false;
            return !is_postal_address_invalid;
        }

        public Boolean isWebUser() {
            if(is_web_user == null) is_web_user = false;
            return is_web_user;
        }

        public Boolean getWebAccountBanned() {
            if(web_account_banned == null) web_account_banned = false;
            return web_account_banned;
        }

        public boolean isActive() {
            return status != null && status == 1;
        }

        public String getDeclaredProduct(){
            String declaredProduct = sku!=null ? sku : sku_1;
            if(declaredProduct != null){
                declaredProduct = declaredProduct.toUpperCase();
            }
            return declaredProduct;
        }


        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }

        public String getBirthdate() {
            return birthdate;
        }

        public void setBirthdate(String birthdate) {
            this.birthdate = birthdate;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCounty() {
            return county;
        }

        public void setCounty(String county) {
            this.county = county;
        }

        public String getStreet_address() {
            return street_address;
        }

        public void setStreet_address(String street_address) {
            this.street_address = street_address;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getError_message() {
            return error_message;
        }

        public void setError_message(String error_message) {
            this.error_message = error_message;
        }
    }
}


