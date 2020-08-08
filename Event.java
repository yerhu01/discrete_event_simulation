
// event representation
class Event implements Comparable {

 public Event(int a_type, double a_time, int a_queue, long a_packetid, double a_arrivaltime) { 
	_type = a_type; time = a_time; 
	queue = a_queue; packetid = a_packetid; arrivaltime = a_arrivaltime;
 }
  
 public double time;
 public int queue; //source, higher or lower queue
 public long packetid;
 public double arrivaltime; //arrival time to source
 private int _type;
 
 public int get_type() { return _type; }
 public double get_time() { return time; }
 public int get_queue(){ return queue;}
 public long get_packetid(){ return packetid;}
 public double get_arrivaltime(){ return arrivaltime;}

 public Event leftlink, rightlink, uplink;

 public int compareTo(Object _cmpEvent ) {
  double _cmp_time = ((Event) _cmpEvent).get_time() ;
  if( this.time < _cmp_time) return -1;
  if( this.time == _cmp_time) return 0;
  return 1;
 }
};