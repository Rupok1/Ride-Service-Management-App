package com.example.ride.Model;

public class CustomerAvailable {

    String name,phone,cartype,rating,serivce,profileImageUrl;

    public CustomerAvailable(String name, String phone, String profileImageUrl) {
        this.name = name;
        this.phone = phone;

        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
