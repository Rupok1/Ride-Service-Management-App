package com.example.ride;

public class User2 {

       String name,email,phone,type,cost;

        public User2(String name, String email, String phone, String type, String cost) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.type = type;
            this.cost = cost;
        }

        public String getCost() {
            return cost;
        }

        public void setCost(String cost) {
            this.cost = cost;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }


}
