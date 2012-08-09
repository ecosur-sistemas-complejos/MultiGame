package mx.ecosur.multigame.manantiales.token
{
import mx.ecosur.multigame.manantiales.token.ManantialesToken;
import mx.ecosur.multigame.manantiales.enum.TokenType;

    [ResourceBundle("ManantialesCommon")]
	public class ModerateToken extends ManantialesToken
	{
		public function ModerateToken()
		{
			super();
			_tooltip = resourceManager.getString("Manantiales", "manantiales.token.moderate");
			_label = "M";
			_type = TokenType.MODERATE;
		}
	}
}
