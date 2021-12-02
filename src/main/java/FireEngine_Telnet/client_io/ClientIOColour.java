/*
 * Copyright 2017 Benjamin James Hook
 * ClientIOColour.java
 */
package FireEngine_Telnet.client_io;

// Mock-static class.
public final class ClientIOColour {
	public static enum COLOURS {
		RESET, BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE, BRIGHTBLACK, BRIGHTRED, BRIGHTGREEN, BRIGHTYELLOW,
		BRIGHTBLUE, BRIGHTMAGENTA, BRIGHTCYAN, BRIGHTWHITE
	}

	private ClientIOColour() {
	}

	public static ClientConnectionOutput showColours() {
		ClientConnectionOutput output = new ClientConnectionOutput(26);
		output.addPart("Foregrounds:", ClientIOColour.COLOURS.WHITE, null);
		output.newLine();
		output.addPart("BLACKBLACKBLACKBLACK", ClientIOColour.COLOURS.BLACK, ClientIOColour.COLOURS.WHITE);
		output.newLine();
		output.addPart("REDREDREDREDREDREDRE", ClientIOColour.COLOURS.RED, null);
		output.newLine();
		output.addPart("GREENGREENGREENGREEN", ClientIOColour.COLOURS.GREEN, null);
		output.newLine();
		output.addPart("YELLOWYELLOWYELLOWYE", ClientIOColour.COLOURS.YELLOW, null);
		output.newLine();
		output.addPart("BLUEBLUEBLUEBLUEBLUE", ClientIOColour.COLOURS.BLUE, null);
		output.newLine();
		output.addPart("MAGENTAMAGENTAMAGENT", ClientIOColour.COLOURS.MAGENTA, null);
		output.newLine();
		output.addPart("CYANCYANCYANCYANCYAN", ClientIOColour.COLOURS.CYAN, null);
		output.newLine();
		output.addPart("WHITEWHITEWHITEWHITE", ClientIOColour.COLOURS.WHITE, null);
		output.newLine();
		output.addPart("BRIGHTBLACKBRIGHTBLA", ClientIOColour.COLOURS.BRIGHTBLACK, null);
		output.newLine();
		output.addPart("BRIGHTREDBRIGHTREDBR", ClientIOColour.COLOURS.BRIGHTRED, null);
		output.newLine();
		output.addPart("BRIGHTGREENBRIGHTGRE", ClientIOColour.COLOURS.BRIGHTGREEN, null);
		output.newLine();
		output.addPart("BRIGHTYELLOWBRIGHTYE", ClientIOColour.COLOURS.BRIGHTYELLOW, null);
		output.newLine();
		output.addPart("BRIGHTBLUEBRIGHTBLUE", ClientIOColour.COLOURS.BRIGHTBLUE, null);
		output.newLine();
		output.addPart("BRIGHTMAGENTABRIGHTM", ClientIOColour.COLOURS.BRIGHTMAGENTA, null);
		output.newLine();
		output.addPart("BRIGHTCYANBRIGHTCYAN", ClientIOColour.COLOURS.BRIGHTCYAN, null);
		output.newLine();
		output.addPart("BRIGHTWHITEBRIGHTWHI", ClientIOColour.COLOURS.BRIGHTWHITE, null);
		output.newLine();
		output.addPart("Backgrounds:", ClientIOColour.COLOURS.WHITE, null);
		output.newLine();
		output.addPart("BLACKBLACKBLACKBLACK", ClientIOColour.COLOURS.WHITE, ClientIOColour.COLOURS.BLACK);
		output.newLine();
		output.addPart("REDREDREDREDREDREDRE", ClientIOColour.COLOURS.BLACK, ClientIOColour.COLOURS.RED);
		output.newLine();
		output.addPart("GREENGREENGREENGREEN", ClientIOColour.COLOURS.BLACK, ClientIOColour.COLOURS.GREEN);
		output.newLine();
		output.addPart("YELLOWYELLOWYELLOWYE", ClientIOColour.COLOURS.BLACK, ClientIOColour.COLOURS.YELLOW);
		output.newLine();
		output.addPart("BLUEBLUEBLUEBLUEBLUE", ClientIOColour.COLOURS.BLACK, ClientIOColour.COLOURS.BLUE);
		output.newLine();
		output.addPart("MAGENTAMAGENTAMAGENT", ClientIOColour.COLOURS.BLACK, ClientIOColour.COLOURS.MAGENTA);
		output.newLine();
		output.addPart("CYANCYANCYANCYANCYAN", ClientIOColour.COLOURS.BLACK, ClientIOColour.COLOURS.CYAN);
		output.newLine();
		output.addPart("WHITEWHITEWHITEWHITE", ClientIOColour.COLOURS.BLACK, ClientIOColour.COLOURS.WHITE);
		return output;
	}
}
