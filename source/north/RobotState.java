package north;

public class RobotState {
   public float posx;
   public float posy;
   public float velx;
   public float vely;
   public float angle;
   
   public RobotState(float posx, float posy, float velx, float vely, float angle) {
      this.posx = posx;
      this.posy = posy;
      this.velx = velx;
      this.vely = vely;
      this.angle = angle;
   }
}