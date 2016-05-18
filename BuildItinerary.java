import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import net.datastructures.Edge;
import net.datastructures.Vertex;

public class BuildItinerary {

	AdjacencyListGraph<TimeAtAirport, Flight> graph;

	public BuildItinerary(ArrayList<Flight> flights) {

		buildGraph(flights);

	}

	private void buildGraph(ArrayList<Flight> flights) {
		graph = new AdjacencyListGraph<TimeAtAirport, Flight>(true); // empty
																		// directed
																		// graph

		// Insert vertices and edges for all flight legs
		Hashtable<TimeAtAirport, Vertex> vertices = new Hashtable<TimeAtAirport, Vertex>();

		for (Flight flightLeg : flights) {
			TimeAtAirport depart = new TimeAtAirport(flightLeg.getAirportDepart(), flightLeg.getTimeDepart());
			TimeAtAirport destin = new TimeAtAirport(flightLeg.getAirportDestin(), flightLeg.getTimeDestin());

			Vertex<TimeAtAirport> sourceV = vertices.get(depart);
			if (sourceV == null) {
				// Source vertex not in graph -- insert
				sourceV = graph.insertVertex(depart);
				vertices.put(depart, sourceV);
			}
			Vertex<TimeAtAirport> destV = vertices.get(destin);
			if (destV == null) {
				// Destination vertex not in graph -- insert
				destV = graph.insertVertex(destin);
				vertices.put(destin, destV);
			}

			graph.insertEdge(sourceV, destV, flightLeg);
		}

		// connect vertices in the same airport in increasing direction of time

		ArrayList<TimeAtAirport> allTAA = new ArrayList<TimeAtAirport>();
		for (Vertex<TimeAtAirport> v : graph.vertices()) {
			allTAA.add(v.getElement());
		}

		Collections.sort(allTAA); // sort according to Comparable TimeAtAirport
									// (first airport the time)

		TimeAtAirport taa1 = allTAA.get(0);
		TimeAtAirport taa2;

		for (int i = 1; i < allTAA.size(); i++) {
			taa2 = allTAA.get(i);
			if (taa1.sameAirport(taa2)) {
				Flight dummyFlight = new Flight("Connection", taa1.getAirport(), taa2.getAirport(), "", "",
						taa1.getTime(), taa2.getTime(), 0);
				graph.insertEdge(vertices.get(taa1), vertices.get(taa2), dummyFlight);
			}
			taa1 = taa2;
		}

	}

	private void displayAllPathInfo(DijkstraAlgorithm.ShortestPathsInfo spInfo) {
		int[] listV = spInfo.getPrevVertex();
		int[] listD = spInfo.getDist();
		Edge[] listE = spInfo.getPrevEdge();

		System.out.println("\n*** Displaying All Shortest Path Info given by DijkstraAlgorithm.ShortestPathsInfo***");
		for (int i = 0; i < spInfo.getNumVertices(); i++) {
			System.out.print("Vertex " + i + ":" + graph.getVertexAtPosition(i).getElement() + " Cost=" + listD[i]);
			if (listV[i] != -1)
				System.out.print(", PrevVertex:" + graph.getVertexAtPosition(listV[i]).getElement() + ",PrevEdge:"
						+ listE[i].getElement());
			else if (listD[i] == 0)
				System.out.print(", PrevVertex:(source)");
			else
				System.out.print(",(Unreachable)");
			System.out.println();
		}
		System.out.println("*** End of Displaying All Shortest Path Information:***\n");
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Flight> cheapestItinerary(String airportDepart, String airportDestin, Time minDepTime,
			Time maxArrTime, boolean verbose) {

		System.out.println("\n>>>Customer requested departure from " + airportDepart + " after " + minDepTime
				+ " arriving at " + airportDestin + " before " + maxArrTime);

		DijkstraAlgorithm dijk = new DijkstraAlgorithm();
		ArrayList<Flight> readflight;
		Vertex<TimeAtAirport> minVertex, maxVertex;
		/**********************
		 * CODE TO BE ADDED HERE
		 ******************************************/
		int[] pathinfo;

		TimeAtAirport destination = new TimeAtAirport(airportDestin, maxArrTime);
		TimeAtAirport departure = new TimeAtAirport(airportDepart, minDepTime);
		ArrayList<TimeAtAirport> eligible_depart_time = new ArrayList<TimeAtAirport>();
		ArrayList<TimeAtAirport> eligible_arrive_time = new ArrayList<TimeAtAirport>();

		ArrayList<Vertex> vertices = new ArrayList<>();
		ArrayList<Vertex> vertices1 = new ArrayList<>();

		for (Vertex<TimeAtAirport> v : graph.vertices()) {
			if ((v.getElement().sameAirport(departure)) && (v.getElement().getTime().compareTo(minDepTime) >= 0)) {
				eligible_depart_time.add(v.getElement());
				vertices.add(v);
			}
			if ((v.getElement().sameAirport(destination)) && (v.getElement().getTime().compareTo(maxArrTime) <= 0)) {
				eligible_arrive_time.add(v.getElement());
				vertices1.add(v);
			}
		}

		Collections.sort(eligible_depart_time);

		
		Collections.sort(eligible_arrive_time);

		minVertex = graph.getVertexAtPosition(0);
		maxVertex = graph.getVertexAtPosition(9);
		for (Vertex<TimeAtAirport> v : graph.vertices()) {
			if ((v.getElement().sameAirport(destination)) && (v.getElement().getTime().compareTo(maxArrTime) <= 0)) {

				if (v.getElement().equals(eligible_arrive_time.get(eligible_arrive_time.size() - 1))) {
					maxVertex = v;

				}
			}

		}
		for (Vertex<TimeAtAirport> v : graph.vertices()) {
			if ((v.getElement().sameAirport(departure)) && (v.getElement().getTime().compareTo(minDepTime) >= 0)) {
				if (v.getElement().equals(eligible_depart_time.get(0))) {
					minVertex = v;

				}

			}
		}

		// minVertex=graph.getVertexAtPosition(0);
		Time minTime = minVertex.getElement().getTime();
		// maxVertex=graph.getVertexAtPosition(9);
		Time maxTime = maxVertex.getElement().getTime();

		// if bounds for arrival and departure do not work
		if ((minTime.compareTo(new Time(25, 00)) == 0) || (maxTime.compareTo(new Time(-1, -1)) == 0))
			return null;

		@SuppressWarnings("unchecked")
		DijkstraAlgorithm.ShortestPathsInfo spInfo = dijk.findShortestPaths(minVertex, graph);
		
		double cost = Double.MAX_VALUE;

		for (Vertex<TimeAtAirport> v : vertices) {
			for (Vertex<TimeAtAirport> t : vertices1) {

				if (spInfo.isReachable(graph.getPositionOfVertex(t))) {
					pathinfo = spInfo.pathFromSourceTo(graph.getPositionOfVertex(t));
					int[] dist = spInfo.getDist();

					if (dist[graph.getPositionOfVertex(maxVertex)] < cost) {
						cost = dist[graph.getPositionOfVertex(maxVertex)];
						

					}

				}
			}

		}

		pathinfo = spInfo.pathFromSourceTo(graph.getPositionOfVertex(minVertex));

		 if (verbose)

		 displayAllPathInfo(spInfo);
		// Itinerary finally print below

		if (verbose) {
			// This is the unclean itinerary that prints wait at an airport as
			// different stops
			System.out.println("Unclean Itinerary: from " + minVertex.getElement() + " to " + maxVertex.getElement()
					+ "\n" + spInfo.stringPathFromSourceToVertex(maxVertex, graph));
		}
		String airport = graph.getVertexAtPosition(pathinfo[pathinfo.length - 1]).getElement().airport;

		// System.out.println("To be implemented by the student using <spInfo>
		// (do not use the String above)");
		System.out.print("Official Itinerary: " + minVertex.getElement() + " to " + maxVertex.getElement() + "\n");

		// System.out.println(minVertex.getElement().);

		// *** Here students must add a second printout of Itinerary, but
		// without the waiting edges
		// *** This must use spInfo path returned by the method below; not
		// parsing the previous output!
		int[] path = spInfo.pathFromSourceTo(graph.getPositionOfVertex(maxVertex));
		String s = "";

		for (int c = 0; c < path.length - 1; c++) {
			Flight f = (Flight) graph
					.getEdge(graph.getVertexAtPosition(path[c]), graph.getVertexAtPosition(path[c + 1])).getElement();
			if (f.costFlight != 0)
				s = s + spInfo.prevEdge[path[c + 1]].getElement() + "\n"
						+ graph.getVertexAtPosition(path[c + 1]).getElement() + "\n";
		}
		System.out.print(s);
		if (cost < 0 || cost > 20000)
			System.out.println("No itineraries available; please widen your search times.");
		else
			System.out.println("Total Cost: " + cost);

		/************* add the missing code here *************************/

		return null;
	}

	public AdjacencyListGraph<TimeAtAirport, Flight> getGraph() {
		return graph;
	}

	@SuppressWarnings("unchecked")
	public void whereMoneyCanGetMe(String airportDepart, Time minDepTime, int dollars, boolean verbose) {

		// Similar to the cheapestItinerary in the determination of the source
		// vertex
		// Using a fixeed dummy value now.
		
		// for part 3 to be added ******************************/
		
		ArrayList<TimeAtAirport> eligible_depart_time = new ArrayList<TimeAtAirport>();

		ArrayList<Vertex> vertices = new ArrayList<>();
		TimeAtAirport departure = new TimeAtAirport(airportDepart, minDepTime);

		for (Vertex<TimeAtAirport> v : graph.vertices()) {
			if ((v.getElement().sameAirport(departure)) && (v.getElement().getTime().compareTo(minDepTime) >= 0)) {
				eligible_depart_time.add(v.getElement());
				vertices.add(v);
			}
		}

		Collections.sort(eligible_depart_time);

		Vertex<TimeAtAirport> minVertex = graph.getVertexAtPosition(0);
		for (Vertex<TimeAtAirport> v : graph.vertices()) {
			if ((v.getElement().sameAirport(departure)) && (v.getElement().getTime().compareTo(minDepTime) >= 0)) {
				if (v.getElement().equals(eligible_depart_time.get(0))) {
					minVertex = v;

				}

			}
		}
		System.out.println();

		DijkstraAlgorithm dijk = new DijkstraAlgorithm();
		DijkstraAlgorithm.ShortestPathsInfo spInfo = dijk.findShortestPaths(minVertex, graph);

		if (verbose)
			displayAllPathInfo(spInfo);
		int[] path = spInfo.getPrevVertex();
		int[] dist = spInfo.getDist();

		
	
		
		System.out.println(">>>> Here you will provide info on where to go under $" + dollars);
		for (int c = 0; c < path.length; c++) {
			if (path[c] >= 0) {
				Vertex<TimeAtAirport> tp = graph.getVertexAtPosition(path[c]);

				Vertex<TimeAtAirport> tp1 = graph.getVertexAtPosition(c);
				{
					if (dist[path[c]] != 0 && dist[path[c]] <= dollars
							&& ((graph.getVertexAtPosition(path[c]).getElement().sameAirport(tp1.getElement()))
									&& dist[path[c]] == dist[c])) {
						System.out.println(graph.getVertexAtPosition(path[c]).getElement());

					}
				}
			}
		}
	}

	public class TimeAtAirport implements Comparable {
		String airport;
		Time time;

		public TimeAtAirport(String airport, Time time) {
			this.airport = airport;
			this.time = time;
		}

		Time getTime() {
			return time;
		}

		String getAirport() {
			return airport;
		}

		boolean sameAirport(TimeAtAirport other) {
			return (this.airport.equals(other.airport));
		}

		@Override
		public int compareTo(Object o) throws ClassCastException {
			if (!(o instanceof TimeAtAirport))
				throw new ClassCastException("A TimeAtAirport object expected.");
			TimeAtAirport otherTAA = (TimeAtAirport) o;
			int result = this.airport.compareTo(otherTAA.airport);
			if (result == 0)
				result = this.time.compareTo(otherTAA.time);
			return result;
		}

		public String toString() {
			return airport + " " + time;
		}

	}

}
