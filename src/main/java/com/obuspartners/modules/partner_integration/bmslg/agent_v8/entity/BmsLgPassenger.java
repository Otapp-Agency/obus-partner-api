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

import jakarta.annotation.PostConstruct;

/**
 * BMSLG Passenger entity representing a passenger from BMSLG system
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
@Table(name = "bmslg_passengers")
public class BmsLgPassenger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 26)
    private String uid;

    // === PASSENGER BASIC INFO ===
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false)
    private String name;
    
    @Size(max = 20)
    @Column(name = "gender")
    private String gender;
    
    @Size(max = 20)
    @Column(name = "category")
    private String category;
    
    @Size(max = 50)
    @Column(name = "passport")
    private String passport;
    
    // === SEAT AND FARE INFO ===
    @NotBlank
    @Size(max = 20)
    @Column(name = "seat_id", nullable = false)
    private String seatId;
    
    @Size(max = 10)
    @Column(name = "seat_type_id")
    private String seatTypeId;
    
    @Size(max = 20)
    @Column(name = "seat_mob")
    private String seatMob;
    
    @Size(max = 100)
    @Column(name = "email")
    private String email;
    
    @Size(max = 20)
    @Column(name = "new_seat_fare")
    private String newSeatFare;

    // === ROUTE INFO (from main passengers data) ===
    @Size(max = 50)
    @Column(name = "apbi_id")
    private String apbiId;
    
    @Size(max = 50)
    @Column(name = "from_id")
    private String fromId;
    
    @Size(max = 50)
    @Column(name = "to_id")
    private String toId;
    
    @Size(max = 20)
    @Column(name = "trvl_dt")
    private String trvlDt;
    
    @Size(max = 50)
    @Column(name = "sub_id")
    private String subId;
    
    @Size(max = 50)
    @Column(name = "tdi_id")
    private String tdiId;
    
    @Size(max = 50)
    @Column(name = "lb_id")
    private String lbId;
    
    @Size(max = 50)
    @Column(name = "pbi_id")
    private String pbiId;
    
    @Size(max = 50)
    @Column(name = "asi_id")
    private String asiId;
    
    @Size(max = 50)
    @Column(name = "ukey")
    private String ukey;
    
    @Size(max = 100)
    @Column(name = "boarding")
    private String boarding;
    
    @Size(max = 100)
    @Column(name = "dropping")
    private String dropping;
    
    @Size(max = 10)
    @Column(name = "boarding_time")
    private String boardingTime;
    
    @Size(max = 10)
    @Column(name = "dropping_time")
    private String droppingTime;

    // === RELATIONSHIPS ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bmslg_booking_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BmsLgBooking bmsLgBooking;

    // === HELPER METHODS ===
    @PostConstruct
    public void init() {
        if (this.uid == null) {
            this.uid = new ULID().nextULID();
        }
    }
}
