# qwerty-jaro-winkler
The Jaro–Winkler distance is one of the ways of measuring the edit-distance between two sequences. Informally, the Jaro distance between two words is the minimum number of single-character transpositions required to change one word into the other.

 The Qwerty-Jaro–Winkler distance is a tweak on top of jaro winkler edit distance where we try to consider distance between keys in keyboard while calculating number of matches.
 
 
	The Jaro distance 
           dj  = {
            0 if m=0
            ⅓(m/|s1| + m/|s2| + (m-t )/m) if m > 0
           }

	Where m is number of matches
		|si| is length of string si 
		T is half of the number of transpositions.

Two characters s1 and s2 are considered matching only if they are the same and not farther than floor( max(|s1|,|s2|)/2 ) -1.

The higher value of jaro distance denotes that strings are more similar and vice-versa.

The Jaro-winkler distance

	dw = dj + (l*p(1-dj))
	Where dw is jaro distance
		L is the length of common prefix at the start of the string up to a maximum of four characters.
		P is a constant scaling factor for how much the score is adjusted upwards for having common prefixes.

GIve example for MARTHA and MARHTA followed by NIKE and NUKE.

Example of NIKE and NUKE using qwerty jaro.

In Jaro–Winkler distance our match will be either 0 or 1 depending on the character’s we compare match or not. In Qwerty-Jaro–Winkler distance if there is no match we try to assign assign a value between 1 and 0, depending on the distance of chars in qwerty keyboard layout.
