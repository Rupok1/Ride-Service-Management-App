package com.example.ride.Model;

public class DriverAvailable {

    String name,phone,cartype,rating,serivce,profileImageUrl,earned,unpaid;

    public DriverAvailable(String name, String phone, String cartype, String rating, String serivce, String profileImageUrl, String earned, String unpaid) {
        this.name = name;
        this.phone = phone;
        this.cartype = cartype;
        this.rating = rating;
        this.serivce = serivce;
        this.profileImageUrl = profileImageUrl;
        this.earned = earned;
        this.unpaid = unpaid;
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

    public String getCartype() {
        return cartype;
    }

    public void setCartype(String cartype) {
        this.cartype = cartype;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getSerivce() {
        return serivce;
    }

    public void setSerivce(String serivce) {
        this.serivce = serivce;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getEarned() {
        return earned;
    }

    public void setEarned(String earned) {
        this.earned = earned;
    }

    public String getUnpaid() {
        return unpaid;
    }

    public void setUnpaid(String unpaid) {
        this.unpaid = unpaid;
    }
}
