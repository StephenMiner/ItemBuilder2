package me.stephenminer.customitems.gunutils;

import java.util.HashMap;

public class GunRecord {
    public static HashMap<String, GunRecord> IDS = new HashMap<>();
    private final String id;
    private double damage, range, decayRange, decayRate, spread, waterDecay, bulletSize;
    private float gunap;

    private int ramTime, projectiles, equipCd, pierce, cd;

    private boolean slowRam, gunOffhand;

    private String powder, shot;


    public GunRecord(String id){
        this.id = id;
        GunRecord.IDS.put(id, this);
    }


    public void setDamage(double damage){ this.damage = damage; }
    public void setRange(double range){ this.range = range; }
    public void setDecayRange(double decayRange){ this.decayRange = decayRange; }
    public void setDecayRate(double decayRate){ this.decayRate = decayRate; }
    public void setSpread(double spread){ this.spread = spread; }
    public void setWaterDecay(double waterDecay){ this.waterDecay = waterDecay; }
    public void setBulletSize(double bulletSize){ this.bulletSize = bulletSize; }

    public void setGunap(float gunap){ this.gunap = gunap; }

    public void setRamTime(int ramTime){ this.ramTime = ramTime; }
    public void setProjectiles(int projectiles){ this.projectiles = projectiles; }
    public void setEquipCd(int equipCd){ this.equipCd = equipCd; }
    public void setPierce(int pierce) { this.pierce = pierce; }
    public void setCd(int cd){ this.cd = cd; }

    public void setSlowRam(boolean slowRam){ this.slowRam = slowRam; }
    public void setGunOffhand(boolean gunOffhand){ this.gunOffhand = gunOffhand; }

    public void setPowder(String powder) { this.powder = powder; }
    public void setShot(String shot){ this.shot = shot; }


    public double damage(){ return damage; }
    public double range(){ return range; }
    public double decayRange(){ return decayRange; }
    public double decayRate(){ return decayRate; }
    public double spread(){ return spread; }
    public double waterDecay(){ return waterDecay; }
    public double bulletSize(){ return bulletSize; }

    public float gunap(){ return gunap; }

    public int ramTime(){ return ramTime; }
    public int projectiles(){ return projectiles; }
    public int equipCd(){ return equipCd; }
    public int pierce(){ return pierce; }
    public int cd(){ return cd; }

    public boolean slowRam(){ return slowRam; }
    public boolean gunOffhand(){ return gunOffhand; }

    public String powder(){ return powder; }
    public String shot(){ return shot; }
}
