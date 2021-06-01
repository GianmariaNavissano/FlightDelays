package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	
	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap;
	private Map<Airport, Airport> visita;
	
	public Model() {
		dao = new ExtFlightDelaysDAO();
		idMap = new HashMap<Integer, Airport>();
		dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) {
		
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//Aggiungo i vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(x, idMap));
		
		//Aggiungo gli archi
		for(Rotta r : dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getA1())){
				if(this.grafo.containsVertex(r.getA2())) {
					DefaultWeightedEdge e = this.grafo.getEdge(r.getA1(), r.getA2());
					if(e == null) {
						Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getN());
					} else {
						double pesoVecchio = this.grafo.getEdgeWeight(e);
						double pesoNuovo = pesoVecchio + r.getN();
						this.grafo.setEdgeWeight(e, pesoNuovo);
					}
				}
			}
		}
		
		System.out.println("Grafo creato\n# vertici : "+grafo.vertexSet().size()+"\n# archi: "+this.grafo.vertexSet().size());
		
		
		
	}

	public Set<Airport> getVertici() {
		return this.grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2){
		List<Airport> percorso = new LinkedList<Airport>();
		
		//Devo visitare il gravo. Lo faccio in ampiezza
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(grafo, a1);
		
		visita = new HashMap<>();
		visita.put(a1, null);
		
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport airport1 = grafo.getEdgeSource(e.getEdge());
				Airport airport2 = grafo.getEdgeTarget(e.getEdge());
				
				if(visita.containsKey(airport1) && !visita.containsKey(airport2)) {
					visita.put(airport2, airport1);
				} else if(visita.containsKey(airport2) && !visita.containsKey(airport1)) {
					visita.put(airport1, airport2);
				}
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		while(it.hasNext()) {
			it.next();
		}
		
		percorso.add(a2);
		Airport step = a2;
		
		while(visita.get(step)!=null) {
			step = visita.get(step);
			percorso.add(step);
		}
		
		return percorso;
	}
}
