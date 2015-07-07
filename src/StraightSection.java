
/***********************************************************************
 *   RacingGame 1.0, a java networked racing game
 *   Copyright (C) 2001  John S Montgomery (john.montgomery@lineone.net)
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ************************************************************************/

import java.awt.Rectangle;

public class StraightSection extends Track.Section {
	private boolean vertical;

	public StraightSection( boolean vertical, int x, int y ) {
		super( x, y );
		this.vertical = vertical;
	}

	public boolean isVertical() {
		return vertical;
	}

	public int type() {
		return STRAIGHT;
	}

	public float distanceToEnd( Vehicle vehicle ) {
		Track.Section next = getNextSection();
		Rectangle nextBounds = next.getBounds();
		Rectangle bounds = getBounds();
		Body.State state = vehicle.currentState();
	
		if ( vertical ) {
			int y = nextBounds.y > bounds.y? bounds.y : bounds.y + bounds.height;
			float dist = y - state.y;
			if ( dist < 0 ) dist = -dist;
			return dist;
		}
		else {
			int x = nextBounds.x > bounds.x? bounds.x : bounds.x + bounds.width;
			float dist = x - state.x;
			if ( dist < 0 ) dist = -dist;
			return dist;
		}

	}

	public boolean intersects( Vehicle vehicle, Body.State state ) {
		Rectangle bounds = getBounds();
		for ( int i = 0; i < state.numpoints; i++ ) {
			float x = state.xpoints[ i ];
			float y = state.ypoints[ i ];
			if ( bounds.contains( (int)x, (int)y ) ) {
				return true;
			}
		}
		return false;
	}

	public void checkCollision( Vehicle vehicle, Body.State state ) {
		// not done yet
		if ( vertical ) {
			Rectangle bounds = getBounds();
			for ( int i = 0; i < state.numpoints; i++ ) {
				float x = state.xpoints[ i ];
				float y = state.ypoints[ i ];
				
				if ( x <= bounds.x ) {
					state.collide( x, y, 1, 0 );
				}
				if ( x >= (bounds.x + bounds.width) ) {
					state.collide( x, y, -1, 0 );
				}
			}		
		}
		else {
			Rectangle bounds = getBounds();
			for ( int i = 0; i < state.numpoints; i++ ) {
				float x = state.xpoints[ i ];
				float y = state.ypoints[ i ];
				if ( y <= bounds.y ) {
					state.collide( x, y, 0, 1 );
				}
				if ( y >= (bounds.y + bounds.height) ) {
					state.collide( x, y, 0, -1 );
				}
			}
		}
	}

}
