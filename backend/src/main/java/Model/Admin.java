package Model;

/**
 * Admin.java
 * ----------
 * Plain Java object mirroring the `admins` table.
 */
public class Admin {

    private int    id;
    private String fullName;
    private String email;
    private String password;       // BCrypt hash
    private String collegeName;
    private String adminKeyHash;   // BCrypt hash of the institution access key

    // ── Constructors ────────────────────────────────────────────────────────────

    public Admin() {}

    /** Constructor used when registering a new admin. */
    public Admin(String fullName, String email, String password,
                 String collegeName, String adminKeyHash) {
        this.fullName     = fullName;
        this.email        = email;
        this.password     = password;
        this.collegeName  = collegeName;
        this.adminKeyHash = adminKeyHash;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────────

    public int    getId()                      { return id; }
    public void   setId(int id)               { this.id = id; }

    public String getFullName()               { return fullName; }
    public void   setFullName(String v)       { this.fullName = v; }

    public String getEmail()                  { return email; }
    public void   setEmail(String v)          { this.email = v; }

    public String getPassword()               { return password; }
    public void   setPassword(String v)       { this.password = v; }

    public String getCollegeName()            { return collegeName; }
    public void   setCollegeName(String v)    { this.collegeName = v; }

    public String getAdminKeyHash()           { return adminKeyHash; }
    public void   setAdminKeyHash(String v)   { this.adminKeyHash = v; }

    @Override
    public String toString() {
        return "Admin{id=" + id + ", email='" + email
             + "', college='" + collegeName + "'}";
    }
}
