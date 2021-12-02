package FireEngine_ClientAPI.client_io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Class to contain an Object representation of a packet of output to be sent to
 * the client/user. Can contain multiple output lines, containing multiple out
 * line parts (including blank lines), with each part containing text,
 * foreground colour and background colour(colours optional).
 *
 * @author Ben Hook
 */
public class ClientConnectionOutput {
	private ArrayList<Client_Connection_Output_Line> lineList;

	/**
	 * No arg constructor with a starting capacity of 5 lines.
	 */
	public ClientConnectionOutput() {
		this(5);
	}

	/**
	 * Takes an initial number for the number of lines in output object. Having a
	 * good guess/knowledge beforehand can avoid unnecessary resizing of array
	 * during use.
	 *
	 * @param guessedSize Guessed number of lines in output
	 */
	public ClientConnectionOutput(int guessedSize) {
		lineList = new ArrayList<>(guessedSize);
		newLine();
	}

	/**
	 * Instantiates and adds first part to the new output object.
	 *
	 * @param text String to add to the client output
	 */
	public ClientConnectionOutput(String text) {
		this(1);
		addPart(text);
	}

	/**
	 * Instantiates and adds first part to the new output object.
	 *
	 * @param text     String to add to the client output
	 * @param colourFG foreground colour
	 * @param colourBG background colour
	 */
	public ClientConnectionOutput(String text, ClientIOColour.COLOURS colourFG, ClientIOColour.COLOURS colourBG) {
		this(1);
		addPart(text, colourFG, colourBG);
	}

	/**
	 * Copy constructor. Returns a deep copy duplicate of the passed
	 * {@link ClientConnectionOutput}, useful in situations such as sending output
	 * to a group of people, where their own prompt etc will be attached before
	 * sending.
	 *
	 * @param copyOutput client output to copy in to a new client output
	 */
	public ClientConnectionOutput(ClientConnectionOutput copyOutput) {
		this(copyOutput.lineList.size());
		addOutput(copyOutput);
	}

	/**
	 * Adds a new line to the output. Can be used to add blank lines for
	 * presentation reasons.
	 */
	public void newLine() {
		synchronized (this) {
			lineList.add(new Client_Connection_Output_Line());
		}
	}

	public void newLine(Boolean addToStart) {
		synchronized (this) {
			if (addToStart == true) {
				lineList.add(0, new Client_Connection_Output_Line());
			} else {
				lineList.add(new Client_Connection_Output_Line());
			}
		}
	}

	/**
	 * Adds a part with null colours.
	 *
	 * @param text text to add to client output
	 */
	public void addPart(String text) {
		synchronized (this) {
			addPart(text, null, null);
		}
	}

	public void addPart(String text, Boolean addToStart) {
		synchronized (this) {
			addPart(text, null, null, true);
		}
	}

	/**
	 * The main function used to add line parts; text and colours, to the output
	 * object.
	 *
	 * @param text     text to add to client output
	 * @param colourFG foreground colour of added text
	 * @param colourBG background colour of added text
	 */
	public void addPart(String text, ClientIOColour.COLOURS colourFG, ClientIOColour.COLOURS colourBG) {
		addPart(text, colourFG, colourBG, false);
	}

	public void addPart(String text, ClientIOColour.COLOURS colourFG, ClientIOColour.COLOURS colourBG,
			Boolean addToStart) {
		synchronized (this) {
			if (lineList.size() == 0) {
				newLine();
			}

			int addPosition;
			if (addToStart == true) {
				addPosition = 0;
				lineList.get(addPosition).addPart(text, colourFG, colourBG, true);
			} else {
				addPosition = lineList.size() - 1;
				lineList.get(addPosition).addPart(text, colourFG, colourBG, false);
			}
		}
	}

	/**
	 * Adds output to the end of host output. Calls
	 * {@link ClientConnectionOutput#addOutput(ClientConnectionOutput, Boolean)}
	 * with addToStart as false.
	 * 
	 * @param copyOutput
	 */
	public void addOutput(ClientConnectionOutput copyOutput) {
		synchronized (this) {
			addOutput(copyOutput, false);
		}
	}

	/**
	 * Does a deep copy to avoid passed output being changed, and changing the
	 * copied output after the fact.
	 * <p>
	 * Can add the passed output to the start of host output.
	 * </p>
	 * <p>
	 * Will add passed output to end of current last host output line if not adding
	 * to start. Add blank line to end of passed output if you want output on a new
	 * line.
	 * </p>
	 * 
	 * @param copyOutput
	 * @param addToStart
	 */
	public void addOutput(ClientConnectionOutput copyOutput, Boolean addToStart) {
		synchronized (this) {
			if (addToStart == true) {
				Collections.reverse(copyOutput.lineList);
				Iterator<Client_Connection_Output_Line> iter = copyOutput.lineList.iterator();
				while (iter.hasNext()) {
					Client_Connection_Output_Line line = iter.next();
					Collections.reverse(line.partList);
					for (FireEngine_ClientAPI.client_io.ClientConnectionOutput.Client_Connection_Output_Line.Client_Connection_Output_Part part : line
							.getParts()) {
						this.addPart(part.getText(), part.getColourFG(), part.getColourBG(), true);
					}
					if (iter.hasNext()) {
						this.newLine(true);
					}
				}
			} else {
				Iterator<Client_Connection_Output_Line> iter = copyOutput.lineList.iterator();
				while (iter.hasNext()) {
					Client_Connection_Output_Line line = iter.next();
					for (FireEngine_ClientAPI.client_io.ClientConnectionOutput.Client_Connection_Output_Line.Client_Connection_Output_Part part : line
							.getParts()) {
						this.addPart(part.getText(), part.getColourFG(), part.getColourBG());
					}
					if (iter.hasNext()) {
						this.newLine();
					}
				}
			}
		}

	}

	/**
	 * Used by the sending client IO to test if output object contains more lines to
	 * send.
	 *
	 * @return Boolean indicating if more lines in client output to send, or not
	 */
	public boolean hasNextLine() {
		synchronized (this) {
			if (!lineList.isEmpty()) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Used by the sending client IO to test if output object contains more parts to
	 * send.
	 *
	 * @return Boolean indicating if more parts to send on current line, or not
	 */
	public boolean hasNextPart() {
		synchronized (this) {
			if (hasNextLine()) {
				if (lineList.get(0).hasNextPart()) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * Used by the sending client IO to get text of current part of current line.
	 *
	 * @return String of text for current part of current line
	 */
	public String getText() {
		synchronized (this) {
			return lineList.get(0).getText();
		}
	}

	/**
	 * Used by the sending client IO to get foreground colour of current part of
	 * current line.
	 *
	 * @return colour of foreground of current part of current line
	 */
	public ClientIOColour.COLOURS getColourFG() {
		synchronized (this) {
			return lineList.get(0).getColourFG();
		}
	}

	/**
	 * Used by the sending client IO to get background colour of current part of
	 * current line.
	 *
	 * @return colour of background of current part of current line
	 */
	public ClientIOColour.COLOURS getColourBG() {
		synchronized (this) {
			return lineList.get(0).getColourBG();
		}
	}

	/**
	 * Used by the sending client IO to move on to next part of the line.
	 */
	public void nextPart() {
		synchronized (this) {
			if (hasNextLine()) {
				lineList.get(0).nextPart();
			}
		}
	}

	/**
	 * Used by the sending client IO to move on to next line of the output object.
	 */
	public void nextLine() {
		synchronized (this) {
			if (hasNextLine()) {
				lineList.remove(0);
			}
		}
	}

	/**
	 *
	 *
	 * @author Ben Hook
	 */
	private class Client_Connection_Output_Line {
		private ArrayList<Client_Connection_Output_Part> partList;

		public Client_Connection_Output_Line() {
			partList = new ArrayList<>();
		}

		public void addPart(String text, ClientIOColour.COLOURS colourFG, ClientIOColour.COLOURS colourBG,
				Boolean addToStart) {
			if (addToStart == true) {
				partList.add(0, new Client_Connection_Output_Part(text, colourFG, colourBG));
			} else {
				partList.add(new Client_Connection_Output_Part(text, colourFG, colourBG));
			}
		}

		public boolean hasNextPart() {
			if (!partList.isEmpty()) {
				return true;
			} else {
				return false;
			}
		}

		public String getText() {
			return partList.get(0).getText();
		}

		public ClientIOColour.COLOURS getColourFG() {
			return partList.get(0).getColourFG();
		}

		public ClientIOColour.COLOURS getColourBG() {
			return partList.get(0).getColourBG();
		}

		public void nextPart() {
			if (!partList.isEmpty()) {
				partList.remove(0);
			}
		}

		public ArrayList<Client_Connection_Output_Part> getParts() {
			return partList;
		}

		private class Client_Connection_Output_Part {
			private String text;
			private ClientIOColour.COLOURS colourFG;
			private ClientIOColour.COLOURS colourBG;

			private Client_Connection_Output_Part(String text, ClientIOColour.COLOURS colourFG,
					ClientIOColour.COLOURS colourBG) {
				this.text = text;
				this.colourFG = colourFG;
				this.colourBG = colourBG;
			}

			public String getText() {
				return this.text;
			}

			public ClientIOColour.COLOURS getColourFG() {
				return this.colourFG;
			}

			public ClientIOColour.COLOURS getColourBG() {
				return this.colourBG;
			}
		}

	}
}
