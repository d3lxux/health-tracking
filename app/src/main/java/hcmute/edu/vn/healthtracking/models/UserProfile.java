package hcmute.edu.vn.healthtracking.models;

public class UserProfile {
    private String name;
    private int age;
    private float height;
    private float weight;
    private String avatarUri;
    public UserProfile(String name, int age, float height, float weight, String avatarUri) {
        this.name = name;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.avatarUri = avatarUri;
    }

    // Getter
    public String getName() { return name; }
    public int getAge() { return age; }
    public float getHeight() { return height; }
    public float getWeight() { return weight; }

    public String getAvatarUri() {
        return avatarUri;
    }
}
