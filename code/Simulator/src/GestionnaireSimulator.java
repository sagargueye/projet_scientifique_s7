/***********************************************************************
 * Module:  Gestionnaire.java
 * Author:  Sagar GUEYE
 * Purpose: Defines the Class Gestionnaire
 ***********************************************************************/

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.json.*;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;


public final class GestionnaireSimulator {
	public static int nb_incendie_max= 5;
	public static Coordonnees genereCoordAleatoir() {
		double minLarg= 45.3531152;
		double maxLarg= 45.8743837;
		double minLong= 4.426158699999951;
		double maxLong= 5.227906;
		double longitude = minLong + (Math.random() * (maxLong - minLong));
		double largitude = minLarg + (Math.random() * (maxLarg - minLarg));
		return new Coordonnees(longitude,largitude);
	}
	public static void declencheIncendieChaque5seconde() {
		System.out.println("=========Declenche incendie==============");
		Timer minuteur = new Timer();
		TimerTask tache = new TimerTask() {
			public void run() {
				System.out.println("Déclenche incendie");
				if(nb_incendie_max >0) {
					Coordonnees c=genereCoordAleatoir();
					int intensite=(int) (1 + (Math.random() * (5 - 1)));
					Incendie i=new Incendie( c, intensite, java.time.LocalDateTime.now() ) ;
					//System.out.println(listObjetIncendieCreee.toString());
					//System.out.println( i.toString() );
					String data="{\"incendie\": [{" + 
							"\"longitude\": "+ c.getLongitude()+"," +
					        "\"latitude\": "+ c.getLatitude()+"," +
					        "\"intensite\": "+intensite+"," +
					        "\"debutIncendie\": \""+i.getDebutIncendie()+"\"" + 
					        "}]}";
					String result=POSTRequest("http://127.0.0.1:8080/new_incendie/",data ) ;
					System.out.println( result );
					nb_incendie_max -=1;
				}
			}
		};
		minuteur.schedule(tache, 0, 5000);
		System.out.println("=========fin incendie==============");
	}
	public static synchronized void getInterventionsAndLinkToIncendie() {
		System.out.println("=========link intervention et incendie from emergency to simulator==============");
		String j_string_interventions=GETRequest("http://127.0.0.1:8080/get_interventions_from_emergency/","") ;
		//System.out.println(j_string_interventions);
	    JSONObject json_obj_interventions = new JSONObject(j_string_interventions);
		//System.out.println(json_obj_interventions);

		for (int c = 0; c < json_obj_interventions.length(); c++) {
			JSONObject json_obj2_interventions = json_obj_interventions.getJSONObject(String.valueOf(c));
			//JSONObject element = json_array_camion.getJSONObject(c);
			JSONObject element=json_obj2_interventions;
			int id_intervention=element.getInt("id_intervention");
			int id_incendie=element.getInt("id_incendie");
			String datedeb= element.getString("debut_intervention");				
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			LocalDateTime debutdateTime = LocalDateTime.parse(datedeb, formatter);
			
			//deploiement des intervention vers bd_simulator
			String json_intrv="{\"id_intervention\":"+id_intervention+", \"debut_intervention\":\""+datedeb+"\"}";
			//System.out.println(json_intrv);
			String deploie_intervention=POSTRequest("http://127.0.0.1:8080/deploie_intervention_to_simulator/",json_intrv );
			//System.out.println(deploie_intervention);
			
			//link incendie to intervention
			String test="{\"id_incendie\":"+id_incendie+", \"id_intervention\":"+id_intervention+"}";
		    //System.out.println(test);
		    String incendie_link_intervention=POSTRequest("http://127.0.0.1:8080/incendieLinkIntervention_for_simulator/",test );
		    //System.out.println(incendie_link_intervention);
			
		}
		System.out.println("________end link__________");
	}
	public static String removeLastChar(String s) {
	    return (s == null || s.length() == 0)
	      ? null
	      : (s.substring(0, s.length() - 1));
	}
	public static Incendie getIncendieByIntervention (ArrayList<Incendie> listIncendie, Intervention i) {
		for ( Incendie o: listIncendie ) {
			if ( o.getIntervention().equals(i) )
				return o;
		}
		return null;
	}

	public static Incendie InListObjectIncendie(ArrayList<Incendie> tableau, int idIncendie) {
		for ( Incendie o: tableau ) {
			if ( o.getIdIncendie()== idIncendie)
				return o;
		}
		return null;
	}
	public static Camion InListObjectCamion(ArrayList<Camion> tableau, int id) {
		for ( Camion o: tableau ) {
			if ( o.getIdCamion()== id)
				return o;
		}
		return null;
	}
	public static Intervention InListObjectIntervention(ArrayList<Intervention> tableau, int id) {
		for ( Intervention o: tableau ) {
			if (  o.getIdIntervention()== id )
				return o;
		}
		return null;
	}

	public static void afficheListCamion( ArrayList<Camion> listCamion ) {
		System.out.println("Affichage des Camion: ");
		for ( Camion o: listCamion ) {
			System.out.println(o.toString());
		}
	}
	public static void afficheListIntervention( ArrayList<Intervention> listIntervention ) {
		System.out.println("Affichage des Intervention: ");
		for ( Intervention o: listIntervention ) {
			System.out.println(o.toString());
		}
	}
	public static void afficheListIncendie( ArrayList<Incendie> listIncendie ) {
		System.out.println("Affichage des incident: ");
		for ( Incendie o: listIncendie ) {
			System.out.println(o.toString());
		}
	}

	public static void gestionEvolutionIncendie() {
		System.out.println("======debut  gestionEvolutionIncendie!======");
		String j_string_camion = GETRequest("http://127.0.0.1:8080/get_list_camion_intervenant/","");
		String j_string_incendie = GETRequest("http://127.0.0.1:8080/get_list_incendie_with_intervention/","");
		String j_string_intervention = GETRequest("http://127.0.0.1:8080/get_current_intervention/","");

		System.out.println(j_string_camion);
		System.out.println(j_string_incendie);
		//System.out.println(j_string_intervention);
		
		ArrayList<Incendie> listObjetIncendie= new ArrayList<Incendie>();
		ArrayList<Intervention> listObjetIntervention= new ArrayList<Intervention>();
		ArrayList<Camion> listObjetCamion= new ArrayList<Camion>();

		//JSONObject json_obj = new JSONObject(j_string_incendie);
		//JSONArray json_array_camion = json_obj.getJSONArray("camions");
		//JSONArray json_array_invention = json_obj.getJSONArray("intervention");
		//JSONArray json_array_incendie = json_obj.getJSONArray("incendie");
		
		
	    JSONObject json_obj_camions = new JSONObject(j_string_camion);
	    JSONObject json_obj_incendies = new JSONObject(j_string_incendie);
	    JSONObject json_obj_intervention = new JSONObject(j_string_intervention);
		//System.out.println(json_obj_camions);

		String json_incendie_edit="{\"incendies\" : [";
		String json_intervention_edit="{\"interventions\" : [";

		int z=0;
		//intervention
		for (int i = 0; i < json_obj_intervention.length(); i++) {
			JSONObject json_obj2_intervention = json_obj_intervention.getJSONObject(String.valueOf(i));
			JSONObject element=json_obj2_intervention;
			int id=element.getInt("id_intervention");

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			String datedeb= element.getString("debut_intervention");				
			LocalDateTime debutdateTime = LocalDateTime.parse(datedeb, formatter);
			String datefin=((element.has("fin_intervention") && !element.isNull("fin_intervention"))) ? element.getString("fin_intervention") : null;
			LocalDateTime findateTime = null;
			if(datefin != null) {
				findateTime = LocalDateTime.parse(datefin, formatter);
			}
			Intervention intervtion = new Intervention ( id,   debutdateTime);
			if(findateTime != null) {
				intervtion.setFinIntervention(findateTime);
			}
			listObjetIntervention.add(intervtion);
		}//end listes intervention

		//afficheListIntervention(listObjetIntervention);

		//incendie
		for (int i = 0; i < json_obj_incendies.length(); i++) {
			JSONObject json_obj2_incendies = json_obj_incendies.getJSONObject(String.valueOf(i));
			JSONObject element = json_obj2_incendies;
			int id=element.getInt("id_incendie");
			int idIntervention=element.getInt("id_intervention");
			int intensite=element.getInt("intensite");

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			String datedeb= element.getString("debut_incendie");
			LocalDateTime debutdateTime = LocalDateTime.parse(datedeb, formatter);
			String datefin=((element.has("fin_incendie") && !element.isNull("fin_incendie"))) ? element.getString("fin_incendie") : null;		
			LocalDateTime findateTime = null;
			if(datefin!= null && !datefin.trim().isEmpty()) {
				findateTime = LocalDateTime.parse(datefin, formatter);
			}
			Double latitude=element.getDouble("latitude");
			Double longitude=element.getDouble("longitude");
			Coordonnees coordonneesIncendie= new Coordonnees(longitude,latitude);

			if( InListObjectIntervention(listObjetIntervention,idIntervention) != null) {
				Intervention intervention = InListObjectIntervention(listObjetIntervention,idIntervention);
				Incendie incdie = new Incendie(id, intervention, coordonneesIncendie, intensite, debutdateTime );
				if(findateTime != null) {
					incdie.setFinIncendie(findateTime);
				}
				listObjetIncendie.add(incdie);
			}
		}//end listes incendie

		//afficheListIncendie(listObjetIncendie);

		//camions
		for (int i = 0; i < json_obj_camions.length(); i++) {
			JSONObject json_obj2_camions = json_obj_camions.getJSONObject(String.valueOf(i));
			JSONObject element = json_obj2_camions;
			
			int idCamion=element.getInt("id_camion");
			int idIntervention=element.getInt("id_intervention");
			String matricule=element.getString("immatriculation");
			String type_camion=element.getString("type_camion");
			double volume=element.getDouble("volume");
			double id_caserne=element.getDouble("id_caserne");

			Coordonnees coordonneesCamion= new Coordonnees(element.getDouble("longitude"),element.getDouble("latitude"));

			//System.out.println(idCamion);
			String result_caserne= GETRequest("http://127.0.0.1:8080/get_coordcaserne?id="+String.valueOf(id_caserne),"");
			//System.out.println(result_caserne);
			JSONObject json_obj_caserne = new JSONObject(result_caserne);
			JSONObject json_obj2_caserne = json_obj_caserne.getJSONObject(String.valueOf(0));
			double latitude_caserne=json_obj2_caserne.getDouble("latitude");
			double longitude_caserne=json_obj2_caserne.getDouble("longitude");
			Coordonnees coordonneesCaserne= new Coordonnees(longitude_caserne, latitude_caserne);

			//pour chaque camion qui est en intervention, 
			//lintensité du feu qui lui est associé, diminue de 1 point
			//si les coordonnees du camion est pareilles que celle de lincendie
			if( InListObjectIntervention(listObjetIntervention,idIntervention) != null) {
				Intervention intervention = InListObjectIntervention(listObjetIntervention,idIntervention);
				Camion c = new Camion ( idCamion, intervention, coordonneesCamion, coordonneesCaserne,  matricule,  volume );
				listObjetCamion.add(c);
				Incendie inc = getIncendieByIntervention(listObjetIncendie, intervention) ;
				//si ya toujours lincendie
				if(inc != null && inc.getFinIncendie()==null) {
					//System.out.println("if 2");
					if(inc.getCoordonnes().equals(coordonneesCamion)) {//les camions sont sur place
						int position=listObjetIncendie.indexOf(inc);
						inc.setIntensite( inc.getIntensite() - 1 );
						if(inc.getIntensite() == 0) {//fin de lincendie
							inc.setFinIncendie(java.time.LocalDateTime.now());
							intervention.setFinIntervention(java.time.LocalDateTime.now());//fin intervention
							int indexIntervention=listObjetIntervention.indexOf(intervention);
							listObjetIntervention.set(indexIntervention,intervention);
							String param= "{\"id_intervention\": "+intervention.getIdIntervention() +", \"fin_intervention\":\""+intervention.getFinIntervention() +"\" }";
							//System.out.println(param);
							String result_intervention=POSTRequest("http://127.0.0.1:8080/edit_intervention/",param ) ;
							//System.out.println(result_intervention);
						}
						listObjetIncendie.set(position,inc);
					}
					json_incendie_edit +="{" + 
											"\"id_incendie\": "+inc.getIdIncendie()+"," + 
											"\"intensite\": "+inc.getIntensite()+"," + 
											"\"id_intervention\": "+inc.getIntervention().getIdIntervention()+"," +  
											"\"debut_incendie\": \""+inc.getDebutIncendie()+"\"," +  
											"\"fin_incendie\": \""+inc.getFinIncendie()+"\"," +  
											"\"longitude\": "+inc.getCoordonnes().getLongitude()+"," + 
											"\"latitude\": "+inc.getCoordonnes().getLatitude()+
										"}," ;
					z++;
				}
				
				
			}

		}//end listes camions
		if(z>0) {
			json_incendie_edit=removeLastChar(json_incendie_edit);
		}
		json_incendie_edit +="]}";
		System.out.println(json_incendie_edit);
		String result=POSTRequest("http://127.0.0.1:8080/edit_incendie/",json_incendie_edit ) ;
		System.out.println( result );
		//afficheListCamion(listObjetCamion);
		//afficheListIncendie(listObjetIncendie);
		System.out.println("=======fin  gestionEvolutionIncendie!======");
	}
	public static synchronized void gestionDeplacementCamions() {
		System.out.println("=========GERER LE DECPLACEMENT==============");
		String j_string_camion = GETRequest("http://127.0.0.1:8080/get_list_camion_intervenant/","");
		String j_string_incendie = GETRequest("http://127.0.0.1:8080/get_list_incendie_with_intervention/","");
		System.out.println(j_string_camion);
		//System.out.println(j_string_incendie);

		ArrayList<Incendie> listObjetIncendie= new ArrayList<Incendie>();
		ArrayList<Intervention> listObjetIntervention= new ArrayList<Intervention>();
		ArrayList<Camion> listObjetCamion= new ArrayList<Camion>();
	
		
	    JSONObject json_obj_camions = new JSONObject(j_string_camion);
	    JSONObject json_obj_incendies = new JSONObject(j_string_incendie);
		//System.out.println(json_obj_camions);

		String json_camion_edit="{\"camions\" : [";
		int z=0;
		//on boucle sur la liste des camions:
		// pour chaque camions partis en intervention et qui n'as pas encore atteint sa destination,
		// sa longitude et sa latitude tendend vers la longitude et la latitude de l'incendie qui lui est associé
		for (int c = 0; c < json_obj_camions.length(); c++) {
			JSONObject json_obj2_camions = json_obj_camions.getJSONObject(String.valueOf(c));
			//JSONObject element = json_array_camion.getJSONObject(c);
			JSONObject element=json_obj2_camions;
			int idCamion=element.getInt("id_camion");
			int idInterventionFromCamion=element.getInt("id_intervention");
			String matricule=element.getString("immatriculation");
			double volume=element.getDouble("volume");
			double idCaserne=element.getDouble("id_caserne");


			double latitude=element.getDouble("latitude");
			double longitude=element.getDouble("longitude");
			Coordonnees coordonneesCamion= new Coordonnees(longitude,latitude);
			String result_caserne= GETRequest("http://127.0.0.1:8080/get_coordcaserne?id="+String.valueOf(idCaserne),"");
			//System.out.println(result_caserne);
			JSONObject json_obj_caserne = new JSONObject(result_caserne);
			JSONObject json_obj2_caserne = json_obj_caserne.getJSONObject(String.valueOf(0));
			double latitude_caserne=json_obj2_caserne.getDouble("latitude");
			double longitude_caserne=json_obj2_caserne.getDouble("longitude");
			Coordonnees coordonneesCaserne= new Coordonnees(longitude_caserne, latitude_caserne);

			//Intervention intervention = InListObjectIntervention(listObjetIntervention,idInterventionFromCamion);// a delete
			//on boucle sur les incendies pour recupere celle lié à ce camion 
			for (int i = 0; i < json_obj_incendies.length(); i++) {
				JSONObject elementIncendie = json_obj_incendies.getJSONObject(String.valueOf(i));
				int idInterventionFromIncendie=elementIncendie.getInt("id_intervention");
				//mm id intervention ==> donc c'est notre incendie
				if(idInterventionFromIncendie == idInterventionFromCamion) {
					//à present on peux recuperer ses coordonnées
					//JSONObject coordonnees=elementIncendie.getJSONObject("coordonnees");
					double latitude_incendie=elementIncendie.getDouble("latitude");
					double longitude_incendie=elementIncendie.getDouble("longitude");
					Coordonnees coordonneesIncendie= new Coordonnees(longitude_incendie,latitude_incendie);

					//on verifie si lincendie est toujours dactualité 
					// si oui on gere l'allé des camions sinon on gere leur retour
					String datefin=((elementIncendie.has("fin_incendie") && !elementIncendie.isNull("fin_incendie"))) ? elementIncendie.getString("fin_incendie") : null;
					if(datefin == null) {//ca brule toujours de partout wahouhh!!
						//mm coordonnees => le camion est deja sur place ==> dans ce cas on fait rien
						//coordonnees differente ==> le camion n'est pas encore arrivé à destination (ctd lieux de lincendie)==>traitement
						if (!coordonneesIncendie.equals(coordonneesCamion)) {
							//longitude
							double difLong= coordonneesIncendie.getLongitude() -  coordonneesCamion.getLongitude();
							/*
							if(Math.abs(difLong)> 0.1) {
								//on lui rajoute/enleve 0.1
								coordonneesCamion.setLongitude(coordonneesCamion.getLongitude() + ((difLong < 0) ? -0.1 : +0.1) );
							}else {
							*/
								coordonneesCamion.setLongitude(coordonneesIncendie.getLongitude());
							//}

							//latitude
							double difLat= coordonneesIncendie.getLatitude() -  coordonneesCamion.getLatitude();
							/*
							if(Math.abs(difLat)> 0.5) {
								//on lui rajoute/enleve 0.5
								coordonneesCamion.setLatitude(coordonneesCamion.getLatitude() + ((difLat < 0) ? -0.5 : +0.5) );
							}else {
							*/
								coordonneesCamion.setLatitude(coordonneesIncendie.getLatitude());
							//}
						}
					}//OMG ya plus de feu (^^)
					//Retour des camions
					else {
						//mm coordonnees que la caserne => le camion est deja retourné à la caserne==>ya pas de traitement 
						//coordonnees differente ==> le camion a quitté lincendie et est deja sur la route de retour à la caserne==> traitement
						if (!coordonneesCaserne.equals(coordonneesCamion)) {
							//longitude
							double difLong= coordonneesCaserne.getLongitude() -  coordonneesCamion.getLongitude();
							/*
							if(Math.abs(difLong)> 0.1) {
								//on lui rajoute/enleve 0.1
								coordonneesCamion.setLongitude(coordonneesCamion.getLongitude() + ((difLong < 0) ? -0.1 : +0.1) );
							}else {
							*/
								coordonneesCamion.setLongitude(coordonneesCaserne.getLongitude());
							//}

							//latitude
							double difLat= coordonneesCaserne.getLatitude() -  coordonneesCamion.getLatitude();
							/*
							if(Math.abs(difLat)> 0.5) {
								//on lui rajoute/enleve 0.5
								coordonneesCamion.setLatitude(coordonneesCamion.getLatitude() + ((difLat < 0) ? -0.5 : +0.5) );
							}else {
							*/
								coordonneesCamion.setLatitude(coordonneesCaserne.getLatitude());
							//}
						}
					}
					//Camion camion = new Camion(idCamion, intervention, coordonneesCamion, coordonneesCaserne,  matricule,  volume );
					json_camion_edit +="{" + 
								"\"id_camion\": "+idCamion+"," + 
								"\"id_intervention\": "+idInterventionFromCamion+"," +  
								"\"longitude\": "+coordonneesCamion.getLongitude()+"," + 
								"\"latitude\": "+coordonneesCamion.getLatitude()+
							"}," ;
					//listObjetCamion.add(camion);
					z++;

				}
			}

		}
		if(z>0 ) {
			json_camion_edit=removeLastChar(json_camion_edit);
		}		
		json_camion_edit +="]}";
		System.out.println(json_camion_edit);
		
		String result_simulator=POSTRequest("http://127.0.0.1:8080/deplacement_camion_for_simulator/",json_camion_edit ) ;
		System.out.println( result_simulator );		
		
		String result=POSTRequest("http://127.0.0.1:8080/deplacement_camion/",json_camion_edit ) ;
		System.out.println( result );
		
		String j_string_camion2 = GETRequest("http://127.0.0.1:8080/get_list_camion_intervenant/","");
		System.out.println( j_string_camion2 );

		System.out.println("___________fin GERER LE DECPLACEMENT___________");
	}
	public static synchronized void gestionRetourCamion() {
		System.out.println("=========GERER LE RETOUR CAMION==============");
		String result = GETRequest("http://127.0.0.1:8080/retour_camions/","");
		System.out.println(result);
		System.out.println("___________fin RETOUR CAMION___________");
	}
	private static String POSTRequest(String urlString,String data)  {
		String returnValue="";
        //System.out.println(data);
        try {
	        URL url = new URL(urlString);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("POST");
	        //conn.setRequestProperty("userId", "a1bcdefgh");
	        conn.setRequestProperty("Accept", "application/json");
	        conn.setDoOutput(true);
	        OutputStream os = conn.getOutputStream();
	        os.write(data.getBytes());
	        os.flush();
	        os.close();
	
	        int responseCode = conn.getResponseCode();
	        System.out.println("POST Response Code :  " + responseCode);
	        System.out.println("POST Response Message : " + conn.getResponseMessage());
	        if (responseCode == HttpURLConnection.HTTP_OK) { //success
	            BufferedReader in = new BufferedReader(new InputStreamReader(
	                    conn.getInputStream()));
	            String inputLine;
	            StringBuffer response = new StringBuffer();
	
	            while ((inputLine = in .readLine()) != null) {
	                response.append(inputLine);
	            } in .close();
	
	            // print result
	            returnValue=response.toString();
	            System.out.println(response.toString());
	        } else {
	        	returnValue="POST NOT WORKED";
	            System.out.println("POST NOT WORKED");
	        }
        } catch (Exception e) {
            System.out.println("Exception in NetClientPost:- " + e);
            return "Exception in NetClientPost:- " + e;
        }
		return returnValue;
	}
	private static String GETRequest(String urlCalling, String data) {
		String result="";
		try {
            URL url = new URL(urlCalling);//your url i.e fetch data from .
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : "
                        + conn.getResponseCode());
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String output;
            while (( output=br.readLine() )!= null) {
            	result += output;
                //System.out.println(output);
            }
            conn.disconnect();

        } catch (Exception e) {
            System.out.println("Exception in NetClientGet:- " + e);
            return "Exception in NetClientGet:- " + e;
        }
		return result;
	}
	public static void main(String[] args) {
		declencheIncendieChaque5seconde();
		getInterventionsAndLinkToIncendie();
		Timer minuteur = new Timer();
		TimerTask tache = new TimerTask() {
			public void run() {
				getInterventionsAndLinkToIncendie();
				gestionDeplacementCamions();
				gestionEvolutionIncendie();
				gestionRetourCamion();
			}
		};minuteur.schedule(tache, 0, 9000);
	}
}
