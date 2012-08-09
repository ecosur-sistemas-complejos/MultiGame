/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.gente.entity {
	
	import mx.collections.ArrayCollection;
	import mx.ecosur.multigame.entity.Move;

	/**
	 * Represents a server side PenteMove object.
	 */
	[RemoteClass (alias="mx.ecosur.multigame.gente.entity.GenteMove")]
	public class GenteMove extends Move {
		
		private var _trias:ArrayCollection;
		
		private var _tesseras:ArrayCollection;
		
		private var _teamColors:ArrayCollection;
		
		private var _searchCount:int;
		
		private var _qualifier:String;
		
		public function GenteMove() {
			super();
		}
		
		public function set trias(trias:ArrayCollection):void{
			_trias = trias;
		}
		
		public function get trias():ArrayCollection{
			return _trias;
		}
		
		public function set tesseras(tesseras:ArrayCollection):void{
			_tesseras = tesseras;
		}
		
		public function get tesseras():ArrayCollection{
			return _tesseras;
		}
		
		public function get teamColors():ArrayCollection {
			return _teamColors;
		}
		
		public function set teamColors(teamColors:ArrayCollection):void {
			_teamColors = teamColors;
		}
		
		public function get searchCount():int {
			return _searchCount;
		}
		
		public function set searchCount(searchCount:int):void {
			_searchCount = searchCount;
		}
		
		public function set qualifier(qualifier:String):void{
			_qualifier = qualifier;
		}
		
		public function get qualifier():String{
			return _qualifier;
		}
	}
}
