package vn.androidhaui.foxtrip.models;

public class OrderItem {
    private String slug;
    private String name;
    private Double price;
    private Integer discount;
    private Double finalPrice;
    private String image;
    private Integer quantity;

    public String getSlug() { return slug; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public Integer getDiscount() { return discount != null ? discount : 0; }
    public Double getFinalPrice() { return finalPrice != null ? finalPrice : 0.0; }
    public String getImage() { return image; }
    public Integer getQuantity() { return quantity != null ? quantity : 0; }
}
