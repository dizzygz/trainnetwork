package NetworkRepresentation;

import java.util.ArrayList;
import java.util.List;

import dnl.utils.text.table.TextTable;

public class InterlockingTableGenerator {
	List<Route> journey = new ArrayList<Route>();
	TextTable tt;
	
	public InterlockingTableGenerator(List<Route> journey)
	{
		this.journey = journey;
		createTable();
	}
	
	public void createTable()
	{
		String[] columnNames = {"ID", "Source", "Destination", "Points", "Signals", "Path", "Conflict"};
		Object[][] data = new String[journey.size() + 1][];
		
		for (int i=0; i < journey.size(); i++)
		{
			
			data[i] = generateSettings(journey.get(i));
			
		}
		
		 tt =  new TextTable(columnNames, data);
	}
	
	public String[] generateSettings(Route r)
	{
		String[] row = new String[7];
		row[0] = "r" + Integer.toString(r.getrId());
		row[1] = "s" + Integer.toString(r.getSourceId());
		row[2] = "s" + Integer.toString(r.getDestId());
		row[5] = r.getPathString();

		
		row[3] = pointSettings(r);
		row[4] = signalSettings(r);
		//row[6] = conflictSettings();

		return row;
	}
	
	public String pointSettings(Route r)
	{
		String pointsString = "";
		if (r.hasPoint())
		{
			//set the point's settings
			if (r.getPoint().pointFacingRouteDirection(r))
			{
				if (r.getSourceOwner().isPlus() && r.getDestOwner().isPlus())
				{
					//The point is plus, its pair is minus
					r.getPoint().setPlus();
					r.getPoint().getPair().setMinus();
					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getpId()) + ":p  ";
					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getPair().getpId()) + ":m  "; 
				}
				
				else
				{
					//The point is minus, its pair is plus
					r.getPoint().setMinus();
					r.getPoint().getPair().setPlus();
					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getpId()) + ":m  ";
					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getPair().getpId()) + ":p  "; 
				}
			}
			
			else 
			{
				if (r.getSourceOwner().isPlus() && r.getDestOwner().isPlus())
				{
					//The point is plus
					r.getPoint().setPlus();
					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getpId()) + ":p  ";
				}
				
				else
				{
					//The point is minus
					r.getPoint().setMinus();
					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getpId()) + ":m  ";
					
				}
			}
		}
		return pointsString;
	}
	
	public String signalSettings(Route r)
	{
		String signalsString = " ";

		if (r.hasPoint())
		{
			
			//Then Flanking Signal (not needed if there are no points in route)
			if (r.getPoint().isPlus())
			{
				if (r.getPoint().pointFacingUp())
				{
					((Block)r.getPoint().getNeighList().get(1)).getSignalDown().setStop();
					signalsString = signalsString + "s" + Integer.toString(((Block)r.getPoint().getNeighList().get(1)).getSignalDown().getSigId()) + " ";
				
				}
				
				else 
				{
					((Block)r.getPoint().getNeighList().get(0)).getSignalUp().setStop();
					signalsString = signalsString + "s" + Integer.toString(((Block)r.getPoint().getNeighList().get(0)).getSignalUp().getSigId()) + " ";
				}
			}
			
			else 
			{
				if (r.getPoint().pointFacingUp())
				{
					((Block)r.getPoint().getNeighList().get(2)).getSignalDown().setStop();
					signalsString = signalsString + "s" + Integer.toString(((Block)r.getPoint().getNeighList().get(2)).getSignalDown().getSigId()) + " ";
				
				}
				
				else 
				{
					((Block)r.getPoint().getNeighList().get(1)).getSignalUp().setStop();
					signalsString = signalsString + "s" + Integer.toString(((Block)r.getPoint().getNeighList().get(1)).getSignalUp().getSigId()) + " ";
				}
			}
		
		}

		
		
		//Path Signals
		if (r.isUp())
		{
			for (Section section : r.getPath() )
			{
				if (section instanceof Block)
				{
					if(r.getDestOwner().getSignalDown() != null)
					{
						((Block) section).getSignalDown().setDown();
						signalsString = signalsString + "s" + Integer.toString(((Block) section).getSignalDown().getSigId()) + " ";

					}
					
					else 
					{
						if (section.equals(r.getPath().get(r.getPath().size() - 1)))
						{
							((Block) r.getDestOwner().getNeighList().get(1)).getSignalDown().setStop();
							signalsString = signalsString + "s" + Integer.toString(((Block) r.getDestOwner().getNeighList().get(1)).getSignalDown().getSigId()) + " ";
						}
					}
				}
			}	
		}
		
		else
		{
			for (Section section : r.getPath() )
			{
				if (section instanceof Block)
				{
					if(r.getDestOwner().getSignalUp() != null)
					{
						((Block) section).getSignalUp().setUp();
						signalsString = signalsString + "s" + Integer.toString(((Block) section).getSignalUp().getSigId()) + " ";

					}
					
					else 
					{
						if (section.equals(r.getPath().get(r.getPath().size() - 1)))
						{
							((Block) r.getDestOwner().getNeighList().get(0)).getSignalUp().setStop();
							signalsString = signalsString + "s" + Integer.toString(((Block) r.getDestOwner().getNeighList().get(0)).getSignalUp().getSigId()) + " ";
						}
					}
				}
			}	
		}

		return signalsString;
	}
	
	public String conflictSettings2(Route r)
	{
		String conflictString = " ";
		
		for (Section section: r.getPath())
		{			
			int routeCounter = 0;
			
			if (journey.get(routeCounter).equals(r))
			{
				if (routeCounter < journey.size() - 1)
				{
					break;
				}
				routeCounter++;
			}
			
			for(int i = 0; i < journey.size(); i++)
			{
				boolean conflictFound = false;
				
				Route route2 = journey.get(routeCounter);
				
				int pathCounter = 0;
				
				//stop searching for conflict if found with another route's path
				while(!conflictFound)
				{
					if (section.equals(route2.getPath().get(pathCounter)))
					{
						System.out.println("Conflict Found");
						conflictFound = true;
						conflictString = conflictString + "r" + Integer.toString(route2.getrId()) + " ";	

					}

					pathCounter++;
				}
				
				routeCounter++;	

			}
			
		}
		
		
		return conflictString;
	}
	
	public String conflictSettings()
	{
		String conflictString = " ";
		int routeCounter = 1; 
		
		for (Route route1: journey)
		{			
			for(int i = routeCounter; i < journey.size(); i++)
			{
				boolean conflictFound = false;
				
				int pathCounter1 = 0;
				Route route2 = journey.get(routeCounter);
				
				//stop searching for conflict if found with another route's path
				while(!conflictFound)
				{
					for (int pathCounter2 = 0; pathCounter2 < route2.getPath().size(); pathCounter2++)
					{
						if (route1.getPath().get(pathCounter1).equals(route2.getPath().get(pathCounter2)))
						{
							conflictFound = true;
							conflictString = conflictString + "r" + Integer.toString(route2.getrId()) + " ";
							

						}

					}
				}
				
				
			}
			
			routeCounter++;	
		}
		
		
		return conflictString;
	}
	
	public void printTable()
	{
		System.out.println("");
		tt.printTable();
	}

	
//	public String[] generateSettings(Route r)
//	{
//		String[] row = new String[7];
//		row[0] = "r" + Integer.toString(r.getrId());
//		row[1] = "s" + Integer.toString(r.getSource().getSigId());
//		row[2] = "s" + Integer.toString(r.getDest().getSigId());
//		String pointsString = "";
//		String signalsString = " ";
//		row[5] = r.getPathString();
//		String conflictString = " ";
//
//		//If the route passes a point
//		if (r.hasPoint())
//		{
//			//set the point's settings
//			if (r.getPoint().pointFacingRouteDirection(r))
//			{
//				if (r.getSource().getOwner().isPlus() && r.getDest().getOwner().isPlus())
//				{
//					//The point is plus, its pair is minus
//					r.getPoint().setPlus();
//					r.getPoint().getPair().setMinus();
//					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getpId()) + ":p  ";
//					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getPair().getpId()) + ":m  "; 
//				}
//				
//				else
//				{
//					//The point is minus, its pair is plus
//					r.getPoint().setMinus();
//					r.getPoint().getPair().setPlus();
//					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getpId()) + ":m  ";
//					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getPair().getpId()) + ":p  "; 
//				}
//			}
//			
//			else 
//			{
//				if (r.getSource().getOwner().isPlus() && r.getDest().getOwner().isPlus())
//				{
//					//The point is plus
//					r.getPoint().setPlus();
//					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getpId()) + ":p  ";
//				}
//				
//				else
//				{
//					//The point is minus
//					r.getPoint().setMinus();
//					pointsString = pointsString + "p" + Integer.toString(r.getPoint().getpId()) + ":m  ";
//				}
//			}
//			
//			//Then Flanking Signal (not needed if there are no points in route)
//			if (r.getPoint().isPlus())
//			{
//				if (r.getPoint().pointFacingUp())
//				{
//					((Block)r.getPoint().getNeighList().get(1)).getSignalDown().setStop();
//					signalsString = signalsString + "s" + Integer.toString(((Block)r.getPoint().getNeighList().get(1)).getSignalDown().getSigId()) + " ";
//				
//				}
//				
//				else 
//				{
//					((Block)r.getPoint().getNeighList().get(0)).getSignalUp().setStop();
//					signalsString = signalsString + "s" + Integer.toString(((Block)r.getPoint().getNeighList().get(0)).getSignalUp().getSigId()) + " ";
//				}
//			}
//			
//			else 
//			{
//				if (r.getPoint().pointFacingUp())
//				{
//					((Block)r.getPoint().getNeighList().get(2)).getSignalDown().setStop();
//					signalsString = signalsString + "s" + Integer.toString(((Block)r.getPoint().getNeighList().get(2)).getSignalDown().getSigId()) + " ";
//				
//				}
//				
//				else 
//				{
//					((Block)r.getPoint().getNeighList().get(1)).getSignalUp().setStop();
//					signalsString = signalsString + "s" + Integer.toString(((Block)r.getPoint().getNeighList().get(1)).getSignalUp().getSigId()) + " ";
//				}
//			}
//			
//			//Path Signals
//			if (r.isUp())
//			{
//				for (Section section : r.getPath() )
//				{
//					if (section instanceof Block)
//					{
//						if(r.getDest().getOwner().getSignalDown() != null)
//						{
//							((Block) section).getSignalDown().setDown();
//							signalsString = signalsString + "s" + Integer.toString(((Block) section).getSignalDown().getSigId()) + " ";
//
//						}
//						
//						else 
//						{
//							if (section.equals(r.getPath().get(r.getPath().size() - 1)))
//							{
//								((Block) r.getDest().getOwner().getNeighList().get(1)).getSignalDown().setStop();
//								signalsString = signalsString + "s" + Integer.toString(((Block) r.getDest().getOwner().getNeighList().get(1)).getSignalDown().getSigId()) + " ";
//							}
//						}
//					}
//				}	
//			}
//			
//			else 
//			{
//				for (Section section : r.getPath() )
//				{
//					if (section instanceof Block)
//					{
//						if(r.getDest().getOwner().getSignalUp() != null)
//						{
//							((Block) section).getSignalUp().setDown();
//							signalsString = signalsString + "s" + Integer.toString(((Block) section).getSignalUp().getSigId()) + " ";
//						}
//						
//						else 
//						{
//							if (section.equals(r.getPath().get(r.getPath().size() - 1)))
//							{
//								((Block) r.getDest().getOwner().getNeighList().get(0)).getSignalUp().setStop();
//								signalsString = signalsString + "s" + Integer.toString(((Block) r.getDest().getOwner().getNeighList().get(0)).getSignalUp().getSigId()) + " ";
//							}
//						}
//
//					}
//				}	
//			}
//			
//			//Conflict
//			//if()
//			
//		}
//		
//		row[3] = pointsString;
//		row[4] = signalsString;
//		row[6] = conflictString;
//
//		return row;
//	}
}
