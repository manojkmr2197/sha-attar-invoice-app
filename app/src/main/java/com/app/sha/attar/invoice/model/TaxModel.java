package com.app.sha.attar.invoice.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class TaxModel {

    @PropertyName("tax_id")
    private String tax_id;

    @PropertyName("hsn_code")
    private String hsn_code;

    @PropertyName("tax_name")
    private String tax_name;

    @PropertyName("tax_percentage")
    private Double tax_percentage;

    @PropertyName("description")
    private String description;

    @PropertyName("is_active")
    private Boolean is_active;

    @PropertyName("owner")
    private String owner;

    @PropertyName("created_at")
    private Long created_at;

    @PropertyName("updated_at")
    private Long updated_at;

    public TaxModel() {
    }

    public TaxModel(String hsn_code, String tax_name, Double tax_percentage, String description) {
        this.hsn_code = hsn_code;
        this.tax_name = tax_name;
        this.tax_percentage = tax_percentage;
        this.description = description;
        this.is_active = true;
    }

    @PropertyName("tax_id")
    public String getTax_id() {
        return tax_id != null ? tax_id : "";
    }

    @PropertyName("tax_id")
    public void setTax_id(String tax_id) {
        this.tax_id = tax_id;
    }

    @PropertyName("hsn_code")
    public String getHsn_code() {
        return hsn_code != null ? hsn_code : "";
    }

    @PropertyName("hsn_code")
    public void setHsn_code(String hsn_code) {
        this.hsn_code = hsn_code;
    }

    @PropertyName("tax_name")
    public String getTax_name() {
        return tax_name != null ? tax_name : "";
    }

    @PropertyName("tax_name")
    public void setTax_name(String tax_name) {
        this.tax_name = tax_name;
    }

    @PropertyName("tax_percentage")
    public Double getTax_percentage() {
        return tax_percentage != null ? tax_percentage : 0.0;
    }

    @PropertyName("tax_percentage")
    public void setTax_percentage(Double tax_percentage) {
        this.tax_percentage = tax_percentage;
    }

    @PropertyName("description")
    public String getDescription() {
        return description != null ? description : "";
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("is_active")
    public Boolean getIs_active() {
        return is_active != null ? is_active : true;
    }

    @PropertyName("is_active")
    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }

    @PropertyName("owner")
    public String getOwner() {
        return owner != null ? owner : "";
    }

    @PropertyName("owner")
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @PropertyName("created_at")
    public Long getCreated_at() {
        return created_at != null ? created_at : 0L;
    }

    @PropertyName("created_at")
    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }

    @PropertyName("updated_at")
    public Long getUpdated_at() {
        return updated_at != null ? updated_at : 0L;
    }

    @PropertyName("updated_at")
    public void setUpdated_at(Long updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return getTax_name() + " - " + getHsn_code() + " (" + getTax_percentage() + "%)";
    }
}
