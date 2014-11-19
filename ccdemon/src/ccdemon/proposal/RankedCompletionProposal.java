package ccdemon.proposal;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.jface.text.link.ProposalPosition;


/**
 * The standard implementation of the <code>ICompletionProposal</code> interface.
 */
public final class RankedCompletionProposal implements ICompletionProposal {

	/** The string to be displayed in the completion proposal popup. */
	private String fDisplayString;
	/** The replacement string. */
	private String fReplacementString;
	/** The replacement offset. */
	private int fReplacementOffset;
	/** The replacement length. */
	private int fReplacementLength;
	/** The cursor position after this proposal has been applied. */
	private int fCursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	private Image fImage;
	/** The context information of this proposal. */
	private IContextInformation fContextInformation;
	/** The additional info of this proposal. */
	private String fAdditionalProposalInfo;
	
	private int rank;
	
	private Position position;

	/**
	 * Creates a new completion proposal based on the provided information. The replacement string is
	 * considered being the display string too. All remaining fields are set to <code>null</code>.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
	 */
	public RankedCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, int rank) {
		this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null, 0);
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based on the provided information.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
	 * @param image the image to display for this proposal
	 * @param displayString the string to be displayed for the proposal
	 * @param contextInformation the context information associated with this proposal
	 * @param additionalProposalInfo the additional information associated with this proposal
	 */
	public RankedCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image, String displayString, IContextInformation contextInformation, String additionalProposalInfo, int rank) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);
		Assert.isTrue(rank >= 0);

		fReplacementString= replacementString;
		fReplacementOffset= replacementOffset;
		fReplacementLength= replacementLength;
		fCursorPosition= cursorPosition;
		fImage= image;
		fDisplayString= displayString;
		fContextInformation= contextInformation;
		fAdditionalProposalInfo= additionalProposalInfo;
		this.rank = rank;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public void apply(IDocument document) {
		try {
			
			//why not working!!
//			document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
//			
//			System.out.println("fReplacementOffset: " + fReplacementOffset + " fReplacementLength: " + fReplacementLength);
			
			document.replace(position.offset, position.length, fReplacementString);
			fReplacementOffset = position.offset;
			fReplacementLength = position.length;
			
			System.out.println("position.offset: " + position.offset + " position.length: " + position.length);
			
			this.position.setLength(fReplacementString.length());
			ProposalPosition pp = (ProposalPosition) position;
			for(ICompletionProposal icp : pp.getChoices()){
				RankedCompletionProposal rcp = (RankedCompletionProposal) icp;
				rcp.setLength(position.length);
			}
		} catch (BadLocationException x) {
			// ignore
		}
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document) {
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return fImage;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		if (fDisplayString != null)
			return fDisplayString;
		return fReplacementString;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		return fAdditionalProposalInfo;
	}

	public int getOffset() {
		return this.fReplacementOffset;
	}
	
	public void setOffset(int fReplacementOffset) {
		this.fReplacementOffset = fReplacementOffset;
	}
	public void setLength(int fReplacementLength) {
		this.fReplacementLength = fReplacementLength;
	}
	
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public String getfReplacementString() {
		return fReplacementString;
	}

	public void setfReplacementString(String fReplacementString) {
		this.fReplacementString = fReplacementString;
	}

}
