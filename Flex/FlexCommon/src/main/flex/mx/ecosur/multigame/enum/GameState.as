/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

/** Enumeration that represents the different game status. */
package mx.ecosur.multigame.enum
{
    import mx.resources.IResourceManager;
    import mx.resources.ResourceManager;

    [ResourceBundle("Commons")]
    public class GameState {
        public static const WAITING:String = "WAITING";
        public static const BEGIN:String = "BEGIN";
        public static const PLAY:String = "PLAY";
        public static const ENDED:String = "ENDED";

        public static function getDescription(state:String):String{
            var ret:String;
            var resourceManager:IResourceManager = ResourceManager.getInstance();

            switch (state){
                case WAITING:
                    ret = resourceManager.getString("Commons","game.waiting");
                break;
                case BEGIN:
                case PLAY:
                    ret = resourceManager.getString("Commons","game.play");
                break;
                case ENDED:
                    ret = resourceManager.getString("Commons","game.ended");
                break;
            }

            return ret;
        }
    }
}
