namespace Offsets64{
	enum Offsets64 {
	    set_Health = 0x11D2870,
	    PlayerFixedUpdate = 0x11D9EBC,
	    WeaponUpdate = 0x1233744,
	    WeaponShoot = 0x1233ADC,
	    
	};
}

namespace Fields64{
	enum Fields64 {
	    playerSpeed = 0xA8, //float
	    killCount = 0xA0, //int
	    deathCount = 0xA4, //int
	    bulletSpeed = 0x38, //int
	    ammo = 0xBC, //int
	    respawnTimer = 0x7C //float
	};
}
