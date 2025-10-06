package com.obuspartners.modules.partner_integration.bmslg.agent_v8.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import de.huxhorn.sulky.ulid.ULID;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.obuspartners.modules.common.domain.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;

/**
 * BMSLG Booking entity representing a booking from BMSLG system
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
@Entity
@Table(name = "bmslg_bookings")
public class BmsLgBooking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;

    // === DEVICE AND LOCATION INFO ===
    @Size(max = 50)
    @Column(name = "imei")
    private String imei;
    
    @Size(max = 20)
    @Column(name = "lat")
    private String lat;
    
    @Size(max = 20)
    @Column(name = "longitude")
    private String longitude;
    
    @Size(max = 50)
    @Column(name = "ip")
    private String ip;
    
    // === AUTHENTICATION AND SYSTEM INFO ===
    @NotBlank
    @Size(max = 50)
    @Column(name = "owner_id", nullable = false)
    private String ownerId;
    
    @Size(max = 10)
    @Column(name = "auth_key")
    private String authKey;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "agent_id", nullable = false)
    private String agentId;
    
    @Size(max = 200)
    @Column(name = "`key`")
    private String key;
    
    // === PLATFORM AND APP INFO ===
    @Size(max = 10)
    @Column(name = "is_from")
    private String isFrom;
    
    @Size(max = 10)
    @Column(name = "pltfm")
    private String pltfm;
    
    @Size(max = 10)
    @Column(name = "lang")
    private String lang;
    
    @Size(max = 20)
    @Column(name = "app_ver")
    private String appVer;
    
    // === PASSENGER DATA (JSON STRING) ===
    @NotBlank
    @Column(name = "passengers", columnDefinition = "TEXT", nullable = false)
    private String passengers;
    
    @Column(name = "return_passengers", columnDefinition = "TEXT")
    private String returnPassengers;
    
    // === CONTACT INFO ===
    @Size(max = 20)
    @Column(name = "phone")
    private String phone;
    
    @Size(max = 100)
    @Column(name = "email")
    private String email;
    
    // === PAYMENT INFO ===
    @Size(max = 10)
    @Column(name = "currency")
    private String currency;
    
    @Size(max = 20)
    @Column(name = "pay_code")
    private String payCode;
    
    @Size(max = 20)
    @Column(name = "pay_phone")
    private String payPhone;
    
    // === TRANSACTION PASSWORD ===
    @Size(max = 200)
    @Column(name = "tran_pass")
    private String tranPass;
    
    // === RELATIONSHIPS ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private com.obuspartners.modules.booking_management.domain.entity.Booking booking;
    
    @OneToMany(mappedBy = "bmsLgBooking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<BmsLgPassenger> bmsLgPassengers = new ArrayList<>();

    // === HELPER METHODS ===
    @PostConstruct
    public void init() {
        if (this.uid == null) {
            this.uid = new ULID().nextULID();
        }
    }

    public void addBmsLgPassenger(BmsLgPassenger passenger) {
        bmsLgPassengers.add(passenger);
        passenger.setBmsLgBooking(this);
    }

    public void removeBmsLgPassenger(BmsLgPassenger passenger) {
        bmsLgPassengers.remove(passenger);
        passenger.setBmsLgBooking(null);
    }
}
