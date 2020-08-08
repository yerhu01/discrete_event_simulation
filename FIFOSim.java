import java.util.*;
class FIFOSim {

// Class Sim variables
public static double Clock, MeanInterArrivalTime, ServiceTime,
        Delay1Mean, Delay1Sigma, Delay2,
		SumEndToEnd;
public static long  SrcQueueLength, SrcNumberInService,
					RouterQueueLength, RouterNumberInService,
					TotalPackets, NumberAtDestination, 
					PacketID, CurrentSeq,
					NumberOutOfOrder, NumberPacketsDropped;
public static int MaxQueueLength;
public static int queuecheck;

public final static int arrival = 1;
public final static int departure = 2;
public final static int source = 1;
public final static int router = 2;

public static EventList FutureEventList;
public static Queue SrcQueue;
public static Queue RouterQueue;
public static Random stream;

public static void main(String argv[]) {

  //MeanInterArrivalTime = 1.0/750.0; //Source  (sec)
  MeanInterArrivalTime = 1.0/1125.0;
  ServiceTime = 1.0/1250.0; //Source and Router transmission time (sec)
  TotalPackets  = 1000000;
  Delay1Mean = 0.01; //source to router delay (sec)
  Delay1Sigma = 0.005;
  //Delay1Mean = 0.05; //source to router delay (sec)
  //Delay1Sigma = 0.01;
  //Delay1Mean = 0.1; //source to router delay (sec)
  //Delay1Sigma = 0.05;
  Delay2 = 0.05; // router to destination delay (50ms)
  MaxQueueLength=10; //Router max queue length
  long seed            = Long.parseLong(argv[0]);

  stream = new Random(seed);           // initialize rng stream
  FutureEventList = new EventList();
  SrcQueue = new Queue();
  RouterQueue = new Queue(MaxQueueLength);
 
  Initialization();

  // Loop until first "TotalPackets" have reached destination
  while(NumberAtDestination < TotalPackets ) {
    Event evt = (Event)FutureEventList.getMin();  // get imminent event
    FutureEventList.dequeue();                    // be rid of it
    Clock = evt.get_time();                       // advance simulation time
    if( evt.get_type() == arrival ) ProcessArrival(evt);
    else  ProcessDeparture(evt);
    }
  ReportGeneration();
 }

 public static void Initialization()   { 
  Clock = 0.0;
  SrcQueueLength = 0;
  RouterQueueLength = 0;
  SrcNumberInService = 0;
  RouterNumberInService = 0;
  SumEndToEnd = 0;
  NumberAtDestination = 0;

  PacketID=0;
  CurrentSeq=0;
  
  SumEndToEnd = 0;
  NumberOutOfOrder=0;
  NumberPacketsDropped=0;
  
  queuecheck = 0;
  
  // create first source arrival event
  double arrivaltime = exponential( stream, MeanInterArrivalTime);
   Event evt = new Event(arrival, arrivaltime, source, PacketID, arrivaltime);
  FutureEventList.enqueue( evt );
 }

 public static void ProcessArrival(Event evt) {
	if(evt.get_queue() == source){ 
		SrcQueue.enqueue(evt); 
		SrcQueueLength++;
		// if the server is idle, fetch the event, do statistics
		// and put into service
		if( SrcNumberInService == 0) {ScheduleDeparture(source, evt.get_packetid(), evt.get_arrivaltime());
		}else{ // server is busy}
		}
		
		// schedule the next source arrival
		PacketID++;
		double interarrivaltime = exponential(stream, MeanInterArrivalTime);
		Event next_arrival = new Event(arrival, Clock+interarrivaltime,source,PacketID,Clock+interarrivaltime);
		FutureEventList.enqueue( next_arrival );
	}else{
		//router
		if(RouterQueueLength < MaxQueueLength){
			RouterQueue.enqueue(evt);
			RouterQueueLength++;
			
			//debugging
			if(RouterQueue.numElements() > queuecheck){
					queuecheck = RouterQueue.numElements();
			}
			
			if(evt.get_packetid() > CurrentSeq){
				//in order
				CurrentSeq = evt.get_packetid();
			}else{
				//out of order
				NumberOutOfOrder++;
			}
			// if the server is idle, fetch the event, do statistics
			// and put into service
			if( RouterNumberInService == 0) {ScheduleDeparture(router,evt.get_packetid(), evt.get_arrivaltime());
			}else{ // server is busy}
			}
		}else{
			//drop packet
			NumberPacketsDropped++;
		}
	}
 }

 public static void ScheduleDeparture(int queue, long id, double arrivaltime) {
  if(queue == source){
	  //schedule source departure
	  Event depart = new Event(departure,Clock+ServiceTime, source, id, arrivaltime);
	  FutureEventList.enqueue( depart );
	  SrcNumberInService = 1;
	  SrcQueueLength--;
  }else{
	//schedule router departure
	  Event depart = new Event(departure,Clock+ServiceTime, router, id, arrivaltime);
	  FutureEventList.enqueue( depart );
	  RouterNumberInService = 1;
	  RouterQueueLength--;
  }
 }

public static void ProcessDeparture(Event e) {
	if(e.get_queue() == source){
		  // get the packet description
		  Event finished = (Event) SrcQueue.dequeue();
		  // if there are packets in the queue then schedule
		  // the departure of the next one
		  if( SrcQueueLength > 0 ){
			Event next = (Event) SrcQueue.peekFront();
			ScheduleDeparture(source, next.get_packetid(), next.get_arrivaltime());
		  }else{
			SrcNumberInService = 0;
		  }
		  
		  double TravelDelay;
		  while (( TravelDelay = normal(stream, Delay1Mean, Delay1Sigma)) < 0 );
		  //Schedule next arrival at router
		  Event next_arrival = new Event(arrival, Clock+TravelDelay, router, 
										finished.get_packetid(), finished.get_arrivaltime());
		  FutureEventList.enqueue( next_arrival );
    }else{
		//router departure
		// get the packet description
		 Event finished = (Event) RouterQueue.dequeue();
		 // if there are packets in the queue then schedule
	     // the departure of the next one
		  if( RouterQueueLength > 0 ){
			Event next = (Event) RouterQueue.peekFront();
			ScheduleDeparture(router, next.get_packetid(), next.get_arrivaltime());
		  }else{
			RouterNumberInService = 0;
		  }
		  
		  // measure the response time and add to the sum
		  SumEndToEnd += Clock - finished.get_arrivaltime() + Delay2;
		  
		  NumberAtDestination++;
	}
 }

public static void ReportGeneration() {
double OutOfOrderRate = (double) NumberOutOfOrder/ (double) TotalPackets;
double AVGPacketDelay  = SumEndToEnd/(double) TotalPackets;
double PacketLossRate = (double) NumberPacketsDropped/(double) TotalPackets;

System.out.println( "FIFO QUEUEING");
System.out.println( "\tMEAN INTERARRIVAL TIME                         " 
	+ MeanInterArrivalTime );
System.out.println( "\tSERVICE TIME                              " 
	+ ServiceTime );
System.out.println( "\tNUMBER OF PACKETS                     " + TotalPackets );
System.out.println(); 
System.out.println( "\tPacket out-of-order rate: " + OutOfOrderRate);
System.out.println( "\tAverage packet delay: " + AVGPacketDelay);
System.out.println( "\tAverage packet loss rate: " + PacketLossRate );

System.out.println( "\tSIMULATION RUNLENGTH                           " + Clock + " SECONDS" );
System.out.println( "\tMAXIMUM LINE LENGTH                            " + MaxQueueLength );
System.out.println(queuecheck);
}

public static double exponential(Random rng, double mean) {
 return -mean*Math.log( rng.nextDouble() );
}

public static double SaveNormal;
public static int  NumNormals = 0;
public static final double  PI = 3.1415927 ;

public static double normal(Random rng, double mean, double sigma) {
        double ReturnNormal;
        // should we generate two normals?
        if(NumNormals == 0 ) {
          double r1 = rng.nextDouble();
          double r2 = rng.nextDouble();
          ReturnNormal = Math.sqrt(-2*Math.log(r1))*Math.cos(2*PI*r2);
          SaveNormal   = Math.sqrt(-2*Math.log(r1))*Math.sin(2*PI*r2);
          NumNormals = 1;
        } else {
          NumNormals = 0;
          ReturnNormal = SaveNormal;
        }
        return ReturnNormal*sigma + mean ;
 }
}

