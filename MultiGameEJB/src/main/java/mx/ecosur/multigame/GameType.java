package mx.ecosur.multigame;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;

public enum GameType {

	CHECKERS, PENTE;

	private RuleBase ruleBase;

	private static Logger logger = Logger.getLogger(GameType.class
			.getCanonicalName());

	public String getNamedQuery() {
		if (this.equals(GameType.PENTE))
			return "getPenteGame";
		else
			return "getGameByType";
	}

	public String getNamedQueryById() {
		if (this.equals(GameType.PENTE))
			return "getPenteGameById";
		else
			return "getGameById";
	}
	
	public String getNamedQueryByTypeAndPlayer(){
		if (this.equals(GameType.PENTE))
			return "getPenteGameByTypeAndPlayer";
		else
			return "getGameByTypeAndPlayer";
	}

	public String getNamedMoveQuery() {
		if (this.equals(GameType.PENTE))
			return "getPenteMoves";
		else
			return "getMoves";
	}

	/**
	 * Gets the ruleBase for a given game type. If the ruleBase has not
	 * previously been created for this game type it is created.
	 */
	public RuleBase getRuleBase() {

		/* Check that rule set has not already been created */
		if (ruleBase != null) {
			return ruleBase;
		}

		try {

			logger.fine("Initializing rule set for type " + this);

			/* Initialize the rules based on the type of game */
			PackageBuilder builder = new PackageBuilder();
			InputStreamReader reader = null;

			switch (this) {
			case CHECKERS:
				reader = new InputStreamReader(this.getClass()
						.getResourceAsStream(
								"/mx/ecosur/multigame/checkers.drl"));

				builder.addPackageFromDrl(reader);

				break;
			case PENTE:
				reader = new InputStreamReader(this.getClass()
						.getResourceAsStream("/mx/ecosur/multigame/gente.drl"));
				builder.addPackageFromDrl(reader);
				break;
			default:
				break;
			}

			if (reader != null)
				reader.close();

			/* Create the ruleBase */
			ruleBase = RuleBaseFactory.newRuleBase();
			ruleBase = RuleBaseFactory.newRuleBase();
			ruleBase.addPackage(builder.getPackage());

			logger.fine("Rule set for type " + this + " added to rulesets.");
			return ruleBase;

		} catch (DroolsParserException e) {
			e.printStackTrace();
			throw new RuntimeException (e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
}
