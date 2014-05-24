package js.basic;

import static js.basic.Tools.*;
import java.util.*;

public final class MyMath {
  public static final float PI = (float) Math.PI;
  public static final float DEGTORAD = (float) (Math.PI / 180);
  public static final float RADTODEG = 1.0f / DEGTORAD;

  public static float sin(float a) {
    return (float) Math.sin(a);
  }
  public static float cos(float a) {
    return (float) Math.cos(a);
  }
  public static void err(Object cause) {
    throw new MathError(cause);
  }
  /**
   * Calculate point on boundary of circle
   * @param origin origin of circle
   * @param angle angle of rotation
   * @param radius radius of circle
   * @param dest destination point, or null
   * @return destination point
   */
  public static FlPoint2 ptOnCircle(FlPoint2 origin, float angle, float radius) {
    return new FlPoint2(origin.x + cos(angle) * radius, origin.y + sin(angle)
        * radius);
  }

  public static class MathError extends RuntimeException {
    public MathError(Object string) {
      super(string == null ? "unknown" : string.toString());
    }
  }
  public static float sqrt(float f) {
    return (float) Math.sqrt(f);
  }

  /**
   * Clamp a value into range
   * @param value
   * @param min
   * @param max
   * @return clamped value
   */
  public static int clamp(int value, int min, int max) {
    if (value < min)
      value = min;
    else if (value > max)
      value = max;
    return value;
  }
  /**
   * Snap a scalar to a grid
   * @param n scalaar
   * @param size size of grid cells (assumed to be square)
   * @return point snapped to nearest cell corner
   */
  public static float snapToGrid(float n, float size) {
    return size * Math.round(n / size);
  }

  public static float floor(float n) {
    return (float) Math.floor(n);
  }
  public static float ceil(float n) {
    return (float) Math.ceil(n);
  }

  /**
   * Clamp a value into range
   * @param value
   * @param min
   * @param max
   * @return clamped value
   */
  public static float clamp(float value, float min, float max) {
    if (value < min)
      value = min;
    else if (value > max)
      value = max;
    return value;
  }

  //  /**
  //   * Convert an angle from degrees to radians
  //   * @param degrees angle in degrees
  //   * @return angle in radians
  //   */
  //  public static float radians(float degrees) {
  //    return degrees * DEGTORAD;
  //  }
  //
  //  /**
  //   * Convert an angle from radians to degrees
  //   * @param radians angle in radians
  //   * @return angle in degrees
  //   */
  //  public static float degrees(float radians) {
  //    return radians * RADTODEG;
  //  }

  /**
   * Generate a random value
   * @param n range
   * @return random value in range [0,n)
   */
  public static float rnd(float n) {
    float d = random.nextFloat();
    return d * n;
  }

  /**
   * Seed the random number generator
   * @param seed 
   */
  public static Random seed(int seed) {
    if (seed == 0)
      random = new Random();
    else
      random = new Random(seed);
    return random;
  }

  /**
   * Generate a random integer
   * @param m range
   * @return random integer in range [0,m)
   */
  public static int rnd(int m) {
    int n = mod(random.nextInt(), m);
    return n;
  }

  /**
   * Normalize an angle by replacing it, if necessary, with an
   * equivalent angle in the range [-PI,PI)
   *
   * @param a  angle to normalize
   * @return an equivalent angle in [-PI,PI)
   */
  public static float angle(float a) {
    return mod(a + PI, PI * 2) - PI;
  }

  /**
   * Normalize an angle by replacing it, if necessary, with an
   * equivalent angle in the range [0,2*PI)
   *
   * @param a  angle to normalize
   * @return an equivalent angle in [0,2*PI)
   */
  public static float angle2(float a) {
    float r = angle(a);
    if (r < 0)
      r += PI * 2;
    return r;
  }
  /**
   * Calculate modulus of a value, with proper treatment of negative values
   * @param value
   * @param divisor
   * @return value - v', where v' is the largest multiple of divisor not
   *  greater than value
   */
  public static int mod(int value, int divisor) {
    value = value % divisor;
    if (value < 0)
      value += divisor;
    return value;
  }

  /**
   * Calculate modulus of a value, with proper treatment of negative values
   * @param value
   * @param divisor
   * @return value - v', where v' is the largest multiple of divisor not
   *  greater than value
   */
  public static float mod(float value, float divisor) {
    return (value - divisor * (float) Math.floor(value / divisor));
  }

  /**
   * Determine sign of an integer
   * @param i
   * @return -1,0,+1
   */
  public static int sign(int i) {
    if (i < 0)
      i = -1;
    else if (i > 0)
      i = 1;
    return i;
  }

  /**
   * Determine the (integer) sign of a value
   * @param i value
   * @return integer sign -1,0,1
   */
  public static int sign(float i) {
    if (i < 0)
      return -1;
    else if (i > 0)
      return 1;
    return 0;
  }

  /**
   * Calculate the angle that a vector makes with the x-axis
   * @param pt0 : start point
   * @param pt1 : end point
   * @return normalized angle [-PI...PI) for vector (pt1 - pt0)
   */
  public static float polarAngle(FlPoint2 pt0, FlPoint2 pt1) {
    return polarAngle(pt0.x, pt0.y, pt1.x, pt1.y);
    //    return Math.atan2(pt1.y - pt0.y, pt1.x - pt0.x);
  }

  /**
   * Calculate the angle that a vector makes with the x-axis
   * @param x0,y0  start point
   * @param x1,y1 end point
   * @return normalized angle [-PI...PI) for vector 
   */
  public static float polarAngle(float x0, float y0, float x1, float y1) {
    return (float) Math.atan2(y1 - y0, x1 - x0);
  }

  /**
   * Calculate the angle that a vector makes with the x-axis
   * @param pt end point (start is origin)
   * @return normalized angle [-PI...PI)  
   */
  public static float polarAngle(FlPoint2 pt) {
    return (float) Math.atan2(pt.y, pt.x);
  }

  /**
   * Determine which side of a line a point is on; floating point version
   * @param ax
   * @param ay first point on line
   * @param bx
   * @param by second point on line
   * @param px
   * @param py point to test
   * @return 0 if the point is on the line containing the ray from a to b,
   *  positive value if it's to the left of this ray, negative if it's to the right
   */
  public static float sideOfLine(float ax, float ay, float bx, float by,
      float px, float py) {
    float area2 = ((bx - ax)) * (py - ay) - ((px - ax)) * (by - ay);
    return area2;
  }
  /**
   * Determine which side of a line a point is on; floating point version
   * @param a first point on line
   * @param b second point on line
   * @param p point to test
   * @return 0 if the point is on the line containing the ray from a to b,
   *  positive value if it's to the left of this ray, negative if it's to the right
   */
  public static float sideOfLine(FlPoint2 a, FlPoint2 b, FlPoint2 p) {
    return sideOfLine(a.x, a.y, b.x, b.y, p.x, p.y);
  }
  
  


  /**
   * Determine distance of point from segment
   * @param pt FPoint2
   * @param l0 FPoint2
   * @param l1 FPoint2
   * @param ptOnSeg  if not null, closest point on segment to point is stored here
   * @return double
   */
  public static float ptDistanceToSegment(FlPoint2 pt, FlPoint2 l0,
      FlPoint2 l1, FlPoint2 ptOnSeg) {

    final boolean db = false;
    float dist = 0;
    // calculate parameter for position on segment
    float t = positionOnSegment(pt, l0, l1);
if (db) 
  pr("ptDistanceToSegment "+pt+" --> "+l0+l1+" t="+f(t));
      
    FlPoint2 cpt = null;
    if (t < 0) {
      cpt = l0;
      dist = FlPoint2.distance(pt, cpt);
      if (ptOnSeg != null)
        ptOnSeg.setLocation(cpt);
    } else if (t > 1) {
      cpt = l1;
      dist = FlPoint2.distance(pt, cpt);
      if (ptOnSeg != null)
        ptOnSeg.setLocation(cpt);
    } else {
      dist = ptDistanceToLine(pt, l0, l1, ptOnSeg);
    }

    return dist;
  }
  /**
   * Determine distance of a point from a line
   * @param pt FPoint2
   * @param e0   one point on line
   * @param e1   second point on line
   * @param closestPt if not null, closest point on line is stored here
   * @return distance 
   */
  public static float ptDistanceToLine(FlPoint2 pt, FlPoint2 e0, FlPoint2 e1,
      FlPoint2 closestPt) {

    /*
     *  Let A = pt - l0
     *      B = l1 - l0
     *      
     *  then
     *  
     *      |A x B| = |A||B| sin t
     *      
     *  and the distance is |AxB| / |B|
     *  
     *  
     *  The closest point is
     *  
     *     l0 + (|A| cos t) / |B|
     */
    float bLength = FlPoint2.distance(e0, e1);
    float dist;
    if (bLength == 0) {
      dist = FlPoint2.distance(pt, e0);
      if (closestPt != null)
        closestPt.setLocation(e0);
    } else {
      float ax = pt.x - e0.x;
      float ay = pt.y - e0.y;
      float bx = e1.x - e0.x;
      float by = e1.y - e0.y;

      float crossProd = bx * ay - by * ax;

      dist = Math.abs(crossProd / bLength);

      if (closestPt != null) {
        float scalarProd = ax * bx + ay * by;
        float t = scalarProd / (bLength * bLength);
        closestPt.set(e0.x + t * bx, e0.y + t * by);
      }
    }
    return dist;
  }
  /**
   * Calculate the parameter for a point on a line
   * @param pt FPoint2, assumed to be on line
   * @param s0 start point of line segment (t = 0.0)
   * @param s1 end point of line segment (t = 1.0)
   * @return t value associated with pt
   */
  public static float positionOnSegment(FlPoint2 pt, FlPoint2 s0, FlPoint2 s1) {

    float sx = s1.x - s0.x;
    float sy = s1.y - s0.y;

    float t = 0;

    float dotProd = (pt.x - s0.x) * sx + (pt.y - s0.y) * sy;
    if (dotProd != 0)
      t = dotProd / (sx * sx + sy * sy);

    return t;
  }
  
/**
* Determine intersection point (if one exists) between two line segments
* @param p1 
* @param p2 endpoints of first segment
* @param q1
* @param q2 endpoints of second segment
* @param iParam if not null, parameters of intersection point returned here
* @return FPoint2 if they properly intersect (if parallel or coincident,
*  or if they intersect outside of either segment range, returns null)
*/
public static FlPoint2 lineSegmentIntersection(FlPoint2 p1, FlPoint2 p2,
   FlPoint2 q1, FlPoint2 q2, float[] iParam) {
 return lineSegmentIntersection(p1.x, p1.y, p2.x, p2.y, q1.x, q1.y, q2.x,
     q2.y, iParam);

}


/**
* Determine intersection point (if one exists) between two line segments
* @param p1x
* @param p1y 
* @param p2x
* @param p2y endpoints of first segment
* @param q1x
* @param q1y
* @param q2x
* @param q2y endpoints of second segment
* @param param if not null, parameters of intersection point returned here
* @return FPoint2 if they properly intersect (if parallel or coincident,
*  or if they intersect outside of either segment range, returns null)
*/
public static FlPoint2 lineSegmentIntersection(float p1x, float p1y,
   float p2x, float p2y, float q1x, float q1y, float q2x, float q2y,
   float[] param) {

 final float EPS = 1e-5f;

 FlPoint2 out = null;
 do {
   float denom = (q2y - q1y) * (p2x - p1x) - (q2x - q1x) * (p2y - p1y);
   float numer1 = (q2x - q1x) * (p1y - q1y) - (q2y - q1y) * (p1x - q1x);
   //double numer2 = (p2x - p1x)*(p1y - q1y) - (p2y -p1y)*(p1x-q1x);
   if (Math.abs(denom) < EPS) {
     break;
   }

   float ua = numer1 / denom;

   float numer2 = (p2x - p1x) * (p1y - q1y) - (p2y - p1y) * (p1x - q1x);

   float ub = numer2 / denom;

   if (param != null) {
     param[0] = ua;
     param[1] = ub;
   }
   if (ua < -EPS || ua > 1 + EPS) {
     break;
   }
   if (ub < -EPS || ub > 1 + EPS) {
     break;
   }

   //double ub = numer2/denom;
   out = new FlPoint2(p1x + ua * (p2x - p1x), p1y + ua * (p2y - p1y));
 } while (false);
 return out;
}


  // seeded random number generator
  private static Random random = new Random(1965);

}
