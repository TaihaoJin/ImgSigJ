/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;

/**
 *
 * @author Taihao
 */
public class JarqueBeraTest {//Downloaded from http://www.planet-source-code.com/vb/scripts/ShowCode.asp?txtCodeId=5345&lngWId=2
	String Source;
	double[] data;
	double mean,variance,varianceMLE,stdDevMLE,stdDev,skew,kurto,myJB;
	double PValue;
        double[][] m_pdChi2Table;

	public JarqueBeraTest(String source, double[] data){
		this.Source=source;
		this.data=data;
		getMean();
		getVariance();
		getVarianceMLE();
		getStanDev();
		getSkewness();
		getKurtosis();
		getJB();
                assignTable();
		this.PValue=GetPValue(this.myJB);
	}

        public double getPValue(){
            return PValue;
        }

	private void getMean(){
		double temp=0;
		for (int i=0;i<data.length;i++){
			temp+=data[i];
		}
		this.mean=temp/data.length;
	}

	private void getVariance(){
		double temp=0;
		for (int i=0;i<data.length;i++){
			temp+=Math.pow(data[i]-this.mean,2.0);
		}
		this.variance=temp/(data.length-1);
	}

	private void getVarianceMLE(){
		double temp=0;
		for (int i=0;i<data.length;i++){
			temp+=Math.pow(data[i]-this.mean,2.0);
		}
		this.varianceMLE=temp/(data.length);
	}

	private void getStanDev(){
		this.stdDev=Math.sqrt(this.variance);
		this.stdDevMLE=Math.sqrt(this.varianceMLE);
	}

	private void getSkewness(){
		double temp=0;
		for (int i=0;i<data.length;i++){
			temp+=Math.pow((data[i]-this.mean),3.0);
		}
		this.skew=temp/(Math.pow(this.stdDevMLE, 3.0)*(data.length));
	}

	private void getKurtosis(){
		double temp=0;
		for (int i=0;i<data.length;i++){
			temp+=Math.pow((data[i]-this.mean),4.0);
		}
		this.kurto=temp/(Math.pow(this.stdDevMLE, 4.0)*(data.length));
	}
        void assignTable(){
            m_pdChi2Table=new double[][] {{0.999, 0.002001}, {0.998, 0.00400401}, {0.997, 0.00600902}, {0.996,
		0.00801604}, {0.995, 0.0100251}, {0.994, 0.0120361}, {
			  0.993, 0.0140492}, {0.992, 0.0160643}, {0.991, 0.0180815}, {0.99,
			0.0201007}, {0.989, 0.0221219}, {0.988, 0.0241452}, {0.987, 0.0261705},
			{0.986, 0.0281978}, {0.985, 0.0302273}, {0.984, 0.0322588}, {0.983,
			0.0342923}, {0.982, 0.0363279}, {0.981, 0.0383656}, {0.98, 0.0404054},
			{0.979, 0.0424473}, {0.978, 0.0444912}, {0.977, 0.0465373}, {0.976,
			0.0485854}, {0.975, 0.0506356}, {0.974, 0.052688}, {0.973, 0.0547424},
			{0.972, 0.0567989}, {0.971, 0.0588576}, {0.97, 0.0609184}, {0.969,
			0.0629813}, {0.968, 0.0650464}, {0.967, 0.0671136}, {
			    0.966, 0.0691829}, {0.965, 0.0712544}, {
			    0.964, 0.073328}, {0.963, 0.0754037}, {
			    0.962, 0.0774817}, {0.961, 0.0795617}, {0.96, 0.081644}, {0.959,
			0.0837284}, {0.958, 0.085815}, {0.957, 0.0879038}, {0.956, 0.0899947}, {0.955,
			   0.0920879}, {0.954, 0.0941832}, {0.953,
			   0.0962808}, {0.952, 0.0983805}, {0.951, 0.100482}, {0.95,
			   0.102587}, {0.949, 0.104693}, {0.948,
			   0.106802}, {0.947, 0.108912}, {0.946, 0.111025}, {0.945, 0.113141}, {
			    0.944, 0.115258}, {0.943, 0.117378}, {0.942, 0.1195}, {0.941,
			  0.121624}, {0.94, 0.123751}, {0.939,
			   0.12588}, {0.938, 0.128011}, {0.937, 0.130144}, {0.936, 0.13228}, {0.935,
			   0.134417}, {0.934, 0.136558}, {0.933, 0.1387}, {0.932, 0.140845}, {0.931,
			   0.142992}, {0.93, 0.145141}, {0.929, 0.147293}, {
			    0.928, 0.149447}, {0.927, 0.151603}, {
			    0.926, 0.153762}, {0.925, 0.155923}, {
			    0.924, 0.158086}, {0.923, 0.160252}, {0.922, 0.16242}, {0.921, 0.16459}, {
			    0.92, 0.166763}, {0.919,
			   0.168938}, {
			    0.918, 0.171116}, {0.917, 0.173296}, {0.916, 0.175478}, {0.915,
			0.177662}, {0.914, 0.179849}, {0.913, 0.182039}, {0.912, 0.184231}, {
			  0.911, 0.186425}, {0.91, 0.188621}, {0.909, 0.19082}, {0.908, 0.193022},
			{0.907, 0.195226}, {0.906, 0.197432}, {0.905, 0.199641}, {0.904, 0.201852},
			{0.903, 0.204065}, {0.902,
			    0.206282}, {0.901, 0.2085}, {0.9, 0.210721}, {0.899, 0.212944}, {0.898,
			0.21517}, {0.897, 0.217399}, {0.896, 0.21963}, {0.895, 0.221863}, {0.894,
			0.224099}, {0.893, 0.226337}, {0.892, 0.228578}, {0.891, 0.230822}, {0.89,
			   0.233068}, {0.889, 0.235316}, {0.888, 0.237567}, {0.887, 0.239821}, {0.886,
			   0.242077}, {0.885, 0.244335}, {0.884, 0.246596}, {0.883, 0.24886}, {0.882,
			  0.251126}, {0.881, 0.253395}, {0.88, 0.255667}, {0.879, 0.257941}, {0.878,
			   0.260217}, {0.877, 0.262497}, {0.876, 0.264778}, {0.875, 0.267063}, {0.874,
			   0.26935}, {0.873, 0.271639}, {0.872, 0.273932}, {0.871, 0.276227}, {0.87,
			   0.278524}, {0.869, 0.280824}, {0.868, 0.283127}, {0.867, 0.285433}, {0.866,
			   0.287741}, {0.865, 0.290052}, {0.864, 0.292365}, {0.863, 0.294681}, {0.862,
			   0.297}, {0.861, 0.299322}, {0.86, 0.301646}, {0.859,
			    0.303973}, {0.858, 0.306302}, {0.857, 0.308635}, {0.856, 0.31097}, {0.855,
			     0.313308}, {0.854, 0.315648}, {0.853,
			   0.317991}, {0.852, 0.320338}, {0.851,
			   0.322686}, {0.85, 0.325038}, {0.849, 0.327392}, {0.848,
			     0.329749}, {0.847, 0.332109}, {0.846,
			     0.334472}, {0.845, 0.336837}, {0.844, 0.339206}, {0.843,
			  0.341577}, {0.842, 0.343951}, {0.841, 0.346327}, {0.84, 0.348707}, {0.839,
			   0.351089}, {
			    0.838, 0.353474}, {0.837, 0.355862}, {0.836, 0.358253}, {0.835,
			0.360647}, {0.834, 0.363044}, {0.833, 0.365443}, {0.832, 0.367846}, {0.831,
			0.370251}, {0.83, 0.372659}, {0.829, 0.37507}, {0.828, 0.377484}, {0.827,
			0.379901}, {0.826, 0.382321}, {0.825, 0.384744}, {0.824, 0.387169}, {0.823,
			0.389598}, {0.822, 0.39203}, {0.821, 0.394464}, {0.82, 0.396902}, {0.819,
			0.399342}, {0.818, 0.401786}, {0.817, 0.404232}, {0.816, 0.406682}, {0.815,
			0.409134}, {0.814, 0.41159}, {0.813, 0.414048}, {0.812, 0.41651}, {0.811,
			0.418974}, {0.81, 0.421442}, {0.809, 0.423913}, {
			  0.808, 0.426386}, {0.807, 0.428863}, {
			  0.806, 0.431343}, {0.805, 0.433826}, {0.804, 0.436312}, {0.803, 0.438801},
			{0.802, 0.441293}, {0.801, 0.443789}, {0.8, 0.446287}, {0.799, 0.448789},
			{0.798, 0.451293}, {0.797, 0.453801}, {0.796, 0.456312}, {0.795, 0.458826},
			{0.794, 0.461344}, {0.793, 0.463864}, {0.792, 0.466388}, {0.791, 0.468915},
			{0.79, 0.471445}, {0.789, 0.473978}, {0.788, 0.476514}, {0.787, 0.479054},
			{0.786, 0.481597}, {0.785, 0.484143}, {0.784, 0.486693}, {0.783,
			   0.489245}, {0.782, 0.491801}, {0.781,
			   0.49436}, {0.78, 0.496923}, {0.779, 0.499488}, {0.778, 0.502058}, {0.777,
			   0.50463}, {0.776, 0.507206}, {0.775, 0.509784}, {0.774, 0.512367}, {0.773,
			  0.514952}, {0.772, 0.517541}, {0.771, 0.520134}, {0.77, 0.52273}, {0.769,
			  0.525329}, {0.768, 0.527931}, {0.767, 0.530537}, {0.766, 0.533146}, {0.765,
			  0.535759}, {0.764, 0.538375}, {0.763, 0.540994}, {0.762, 0.543617}, {0.761,
			  0.546244}, {0.76, 0.548874}, {0.759, 0.551507}, {0.758, 0.554144}, {0.757,
			   0.556784}, {0.756, 0.559428}, {0.755, 0.562075}, {0.754, 0.564726},
			{0.753, 0.56738}, {0.752, 0.570038}, {0.751, 0.572699}, {0.75, 0.575364}, {
			    0.749, 0.578033}, {0.748, 0.580705}, {
			    0.747, 0.58338}, {0.746, 0.586059}, {0.745, 0.588742}, {0.744, 0.591428},
			{0.743, 0.594118}, {0.742, 0.596812}, {0.741, 0.599509}, {0.74, 0.60221},
			{0.739, 0.604915}, {
			  0.738, 0.607623}, {0.737, 0.610335}, {0.736, 0.61305}, {0.735,
			   0.61577}, {0.734, 0.618493}, {0.733, 0.621219}, {0.732,
			     0.62395}, {0.731, 0.626684}, {0.73,
			    0.629421}, {0.729, 0.632163}, {0.728, 0.634908}, {0.727, 0.637658},
			{0.726, 0.640411}, {0.725, 0.643167}, {0.724, 0.645928}, {0.723, 0.648692},
			{0.722, 0.65146}, {0.721, 0.654232}, {0.72, 0.657008}, {0.719, 0.659788},
			{0.718, 0.662571}, {0.717, 0.665359}, {0.716, 0.66815}, {0.715, 0.670945},
			{0.714, 0.673745}, {0.713, 0.676548}, {0.712, 0.679355}, {0.711,
			    0.682166}, {0.71, 0.684981}, {0.709, 0.6878}, {0.708, 0.690622}, {0.707,
			0.693449}, {0.706, 0.69628}, {0.705, 0.699115}, {0.704,
			    0.701954}, {0.703, 0.704797}, {0.702, 0.707644}, {0.701, 0.710495}, {0.7,
			    0.71335}, {0.699, 0.716209}, {0.698, 0.719072}, {0.697, 0.72194}, {0.696,
			    0.724811}, {0.695, 0.727687}, {0.694,
			    0.730567}, {0.693, 0.733451}, {0.692,
			    0.736339}, {0.691, 0.739231}, {0.69, 0.742127}, {0.689, 0.745028},
			{0.688, 0.747933}, {0.687, 0.750842}, {0.686, 0.753755}, {0.685, 0.756673},
			{0.684, 0.759595}, {0.683, 0.762521}, {0.682, 0.765451}, {0.681,
			    0.768386}, {0.68, 0.771325}, {0.679,
			    0.774268}, {0.678, 0.777216}, {0.677,
			    0.780168}, {0.676, 0.783124}, {0.675, 0.786085}, {0.674, 0.78905}, {
			    0.673, 0.79202}, {0.672, 0.794994}, {
			    0.671, 0.797972}, {0.67, 0.800955}, {0.669, 0.803942}, {0.668, 0.806934},
			{0.667, 0.80993}, {0.666, 0.812931}, {0.665, 0.815936}, {0.664, 0.818946},
			{0.663, 0.821961}, {0.662, 0.824979}, {0.661, 0.828003}, {0.66, 0.831031},
			{0.659, 0.834063}, {0.658, 0.837101}, {0.657, 0.840143}, {0.656, 0.843189},
			{0.655, 0.84624}, {0.654, 0.849296}, {0.653,
			    0.852356}, {0.652, 0.855421}, {0.651,
			    0.858491}, {0.65, 0.861566}, {0.649, 0.864645}, {0.648, 0.867729},
			{0.647, 0.870818}, {0.646, 0.873912}, {0.645, 0.87701}, {0.644, 0.880113},
			{0.643, 0.883221}, {0.642, 0.886334}, {0.641, 0.889452}, {0.64, 0.892574},
			{0.639, 0.895702}, {0.638, 0.898834}, {0.637, 0.901971}, {0.636, 0.905113},
			{0.635, 0.908261}, {
			  0.634, 0.911413}, {0.633, 0.91457}, {0.632, 0.917732}, {0.631, 0.920899},
			{0.63, 0.924071}, {0.629, 0.927248}, {0.628, 0.93043}, {
			    0.627, 0.933617}, {0.626, 0.93681}, {0.625, 0.940007}, {0.624, 0.94321}, {
			    0.623, 0.946418}, {0.622, 0.94963}, {
			    0.621, 0.952848}, {0.62, 0.956072}, {0.619,
			   0.9593}, {0.618, 0.962534}, {0.617, 0.965773}, {0.616, 0.969017}, {0.615,
			   0.972266}, {0.614, 0.975521}, {0.613, 0.978781}, {0.612, 0.982046}, {0.611,
			   0.985317}, {0.61, 0.988593}, {0.609, 0.991874}, {0.608, 0.995161}, {0.607,
			  0.998453}, {0.606, 1.00175}, {0.605,
			   1.00505}, {0.604, 1.00836}, {0.603, 1.01168}, {0.602,
			    1.015}, {0.601, 1.01832}, {0.6, 1.02165}, {0.599, 1.02499}, {0.598,
			1.02833}, {0.597, 1.03168}, {
			  0.596, 1.03503}, {0.595, 1.03839}, {0.594, 1.04175}, {
			    0.593, 1.04512}, {0.592, 1.0485}, {0.591, 1.05188}, {0.59,
			   1.05527}, {0.589, 1.05866}, {0.588, 1.06206}, {0.587, 1.06546}, {0.586,
			   1.06887}, {0.585, 1.07229}, {0.584,
			   1.07571}, {0.583, 1.07914}, {0.582, 1.08257}, {
			    0.581, 1.08601}, {0.58, 1.08945}, {0.579, 1.09291}, {0.578, 1.09636},
			{0.577, 1.09983}, {0.576, 1.1033}, {0.575, 1.10677}, {
			  0.574, 1.11025}, {0.573, 1.11374}, {0.572, 1.11723}, {0.571, 1.12073}, {
			  0.57, 1.12424}, {0.569, 1.12775}, {0.568, 1.13127}, {0.567, 1.13479},
			{0.566, 1.13832}, {0.565, 1.14186}, {0.564, 1.1454}, {0.563,
			    1.14895}, {0.562, 1.15251}, {0.561, 1.15607}, {0.56, 1.15964}, {0.559,
			    1.16321}, {0.558, 1.16679}, {0.557, 1.17038}, {0.556, 1.17397}, {0.555,
			    1.17757}, {0.554, 1.18118}, {0.553, 1.18479}, {0.552, 1.18841}, {0.551,
			    1.19204}, {0.55, 1.19567}, {0.549, 1.19931}, {0.548, 1.20296}, {0.547,
			    1.20661}, {0.546, 1.21027}, {0.545, 1.21394}, {0.544, 1.21761}, {0.543,
			    1.22129}, {0.542, 1.22498}, {0.541, 1.22867}, {0.54, 1.23237}, {0.539,
			    1.23608}, {0.538, 1.23979}, {0.537, 1.24351}, {0.536, 1.24724}, {0.535,
			    1.25098}, {0.534, 1.25472}, {0.533, 1.25847}, {0.532, 1.26222}, {0.531,
			    1.26599}, {0.53, 1.26976}, {0.529, 1.27353}, {0.528, 1.27732}, {0.527,
			    1.28111}, {0.526, 1.28491}, {0.525, 1.28871}, {0.524, 1.29253}, {0.523,
			    1.29635}, {0.522, 1.30018}, {0.521, 1.30401}, {0.52, 1.30785}, {0.519,
			    1.3117}, {0.518, 1.31556}, {0.517, 1.31942}, {0.516, 1.3233}, {0.515,
			    1.32718}, {0.514, 1.33106}, {0.513, 1.33496}, {0.512, 1.33886}, {0.511,
			    1.34277}, {0.51, 1.34669}, {0.509, 1.35061}, {0.508, 1.35455}, {0.507,
			    1.35849}, {0.506, 1.36244}, {0.505, 1.36639}, {
			    0.504, 1.37036}, {0.503, 1.37433}, {0.502, 1.37831}, {0.501, 1.3823}, {
			    0.5, 1.38629}, {0.499, 1.3903}, {0.498, 1.39431}, {0.497,
			   1.39833}, {0.496, 1.40236}, {0.495,
			   1.4064}, {0.494, 1.41044}, {0.493, 1.41449}, {
			    0.492, 1.41855}, {0.491, 1.42262}, {0.49, 1.4267}, {0.489, 1.43079}, {
			    0.488, 1.43488}, {0.487, 1.43898}, {0.486, 1.44309}, {0.485, 1.44721}, {
			    0.484, 1.45134}, {0.483, 1.45548}, {0.482, 1.45962}, {0.481, 1.46378},
			{0.48, 1.46794}, {0.479, 1.47211}, {0.478, 1.47629}, {0.477, 1.48048}, {
			    0.476, 1.48467}, {0.475, 1.48888}, {0.474, 1.4931}, {0.473, 1.49732},
			{0.472, 1.50155}, {0.471, 1.50579}, {0.47, 1.51005}, {
			  0.469, 1.51431}, {0.468, 1.51857}, {0.467, 1.52285}, {0.466, 1.52714}, {
			  0.465, 1.53144}, {0.464, 1.53574}, {0.463, 1.54006}, {0.462, 1.54438}, {
			  0.461, 1.54871}, {0.46, 1.55306}, {0.459, 1.55741}, {0.458, 1.56177},
			{0.457, 1.56614}, {0.456, 1.57052}, {0.455, 1.57492}, {0.454,
			  1.57932}, {0.453, 1.58373}, {0.452, 1.58815}, {0.451, 1.59258}, {0.45,
			   1.59702}, {0.449, 1.60146}, {
			  0.448, 1.60592}, {0.447, 1.61039}, {0.446,
			    1.61487}, {0.445, 1.61936}, {0.444, 1.62386}, {0.443, 1.62837}, {0.442,
			    1.63289}, {0.441, 1.63742}, {0.44, 1.64196}, {0.439, 1.64651}, {0.438,
			1.65107}, {0.437, 1.65564}, {0.436, 1.66023}, {0.435, 1.66482}, {0.434,
			1.66942}, {0.433, 1.67404}, {0.432, 1.67866}, {0.431, 1.68329}, {0.43,
			    1.68794}, {0.429, 1.6926}, {0.428, 1.69726}, {0.427, 1.70194}, {0.426,
			1.70663}, {0.425, 1.71133}, {0.424, 1.71604}, {0.423, 1.72077}, {0.422,
			1.7255}, {0.421, 1.73024}, {0.42, 1.735}, {0.419, 1.73977}, {0.418, 1.74455},
			{0.417, 1.74934}, {0.416, 1.75414}, {0.415, 1.75895}, {0.414, 1.76378},
			{0.413, 1.76862}, {0.412, 1.77346}, {0.411, 1.77832}, {0.41, 1.7832}, {0.409,
			1.78808}, {0.408, 1.79298}, {0.407, 1.79788}, {0.406, 1.8028}, {0.405,
			1.80774}, {0.404, 1.81268}, {0.403, 1.81764}, {0.402, 1.82261}, {
			    0.401, 1.82759}, {0.4, 1.83258}, {0.399, 1.83759}, {0.398,
			   1.84261}, {0.397, 1.84764}, {0.396,
			   1.85268}, {0.395, 1.85774}, {0.394, 1.86281}, {
			    0.393, 1.86789}, {0.392, 1.87299}, {
			    0.391, 1.8781}, {0.39, 1.88322}, {0.389, 1.88835}, {
			  0.388, 1.8935}, {0.387, 1.89866}, {0.386, 1.90384}, {0.385, 1.90902},
			{0.384, 1.91423}, {0.383, 1.91944}, {0.382, 1.92467}, {0.381, 1.92991},
			{0.38, 1.93517}, {0.379, 1.94044}, {0.378, 1.94572}, {0.377, 1.95102}, {
			  0.376, 1.95633}, {0.375, 1.96166}, {
			  0.374, 1.967}, {0.373, 1.97235}, {0.372, 1.97772}, {
			    0.371, 1.98311}, {0.37, 1.9885}, {0.369, 1.99392}, {0.368, 1.99934}, {
			    0.367, 2.00479}, {0.366, 2.01024}, {0.365, 2.01572}, {0.364, 2.0212}, {
			    0.363, 2.0267}, {0.362, 2.03222}, {
			    0.361, 2.03775}, {0.36, 2.0433}, {0.359, 2.04887}, {0.358,
			   2.05444}, {0.357, 2.06004}, {0.356, 2.06565}, {
			    0.355, 2.07127}, {0.354, 2.07692}, {0.353, 2.08257}, {0.352, 2.08825}, {
			    0.351, 2.09394}, {
			    0.35, 2.09964}, {0.349, 2.10537}, {0.348, 2.11111}, {0.347, 2.11686},
			{0.346, 2.12263}, {0.345, 2.12842}, {0.344, 2.13423}, {0.343, 2.14005},
			{0.342, 2.14589}, {0.341, 2.15175}, {0.34, 2.15762}, {0.339, 2.16351},
			{0.338, 2.16942}, {0.337, 2.17534}, {0.336, 2.18129}, {0.335, 2.18725},
			{0.334, 2.19323}, {0.333, 2.19923}, {0.332,
			   2.20524}, {0.331, 2.21127}, {0.33, 2.21733}, {0.329,
			    2.2234}, {0.328, 2.22948}, {0.327, 2.23559}, {0.326,
			   2.24172}, {0.325, 2.24786}, {0.324, 2.25402}, {0.323, 2.26021}, {0.322,
			   2.26641}, {0.321, 2.27263}, {0.32, 2.27887}, {0.319, 2.28513}, {0.318,
			  2.29141}, {0.317, 2.29771}, {0.316, 2.30403}, {0.315, 2.31037}, {0.314,
			  2.31672}, {0.313, 2.3231}, {0.312,
			   2.3295}, {0.311, 2.33592}, {0.31, 2.34237}, {0.309,
			    2.34883}, {0.308, 2.35531}, {0.307, 2.36182}, {0.306,
			   2.36834}, {0.305, 2.37489}, {0.304, 2.38146}, {0.303, 2.38804}, {0.302,
			   2.39466}, {0.301, 2.40129}, {0.3, 2.40795}, {0.299,
			    2.41462}, {0.298, 2.42132}, {0.297, 2.42805}, {
			    0.296, 2.43479}, {0.295, 2.44156}, {0.294, 2.44835}, {0.293, 2.45517}, {
			    0.292, 2.462}, {0.291, 2.46886}, {0.29, 2.47575}, {0.289,
			  2.48266}, {0.288, 2.48959}, {0.287, 2.49655}, {0.286, 2.50353}, {0.285,
			  2.51053}, {0.284, 2.51756}, {0.283, 2.52462}, {
			    0.282, 2.5317}, {0.281, 2.5388}, {0.28, 2.54593}, {0.279, 2.55309}, {
			    0.278, 2.56027}, {0.277, 2.56748}, {
			    0.276, 2.57471}, {0.275, 2.58197}, {0.274, 2.58925}, {0.273, 2.59657},
			{0.272, 2.60391}, {0.271, 2.61127}, {0.27, 2.61867}, {0.269, 2.62609},
			{0.268, 2.63354}, {0.267, 2.64101}, {0.266, 2.64852}, {0.265, 2.65605},
			{0.264, 2.66361}, {0.263, 2.6712}, {0.262, 2.67882}, {0.261, 2.68647}, {0.26,
			2.69415}, {0.259, 2.70185}, {0.258, 2.70959}, {0.257, 2.71736}, {0.256,
			2.72516}, {0.255, 2.73298}, {0.254, 2.74084}, {0.253, 2.74873}, {0.252,
			2.75665}, {0.251, 2.7646}, {0.25, 2.77259}, {0.249, 2.7806}, {0.248,
			2.78865}, {0.247, 2.79673}, {0.246, 2.80485}, {0.245, 2.81299}, {
			  0.244, 2.82117}, {0.243, 2.82939}, {
			  0.242, 2.83764}, {0.241, 2.84592}, {0.24, 2.85423}, {0.239, 2.86258},
			{0.238, 2.87097}, {0.237, 2.87939}, {0.236,
			    2.88785}, {0.235, 2.89634}, {0.234, 2.90487}, {0.233, 2.91343}, {0.232,
			    2.92204}, {0.231, 2.93068}, {0.23, 2.93935}, {0.229, 2.94807}, {0.228,
			    2.95682}, {0.227, 2.96561}, {0.226, 2.97444}, {0.225,
			    2.98331}, {0.224, 2.99222}, {0.223, 3.00117}, {0.222, 3.01016}, {0.221,
			    3.01919}, {0.22, 3.02826}, {0.219, 3.03737}, {0.218, 3.04652}, {0.217,
			  3.05572}, {0.216, 3.06495}, {0.215, 3.07423}, {0.214, 3.08356}, {0.213,
			  3.09293}, {0.212, 3.10234}, {0.211,
			  3.11179}, {0.21, 3.1213}, {0.209, 3.13084}, {0.208,
			    3.14043}, {0.207, 3.15007}, {0.206, 3.15976}, {0.205, 3.16949}, {0.204,
			    3.17927}, {0.203, 3.1891}, {0.202, 3.19898}, {0.201,
			   3.2089}, {0.2, 3.21888}, {0.199, 3.2289}, {0.198,
			    3.23898}, {0.197, 3.2491}, {0.196, 3.25928}, {0.195, 3.26951}, {0.194,
			3.27979}, {0.193, 3.29013}, {0.192, 3.30052}, {0.191, 3.31096}, {0.19,
			3.32146}, {0.189, 3.33202}, {0.188, 3.34263}, {0.187, 3.35329}, {0.186,
			3.36402}, {0.185, 3.3748}, {0.184, 3.38564}, {0.183, 3.39654}, {
			    0.182, 3.4075}, {0.181, 3.41852}, {0.18, 3.4296}, {0.179,
			   3.44074}, {0.178, 3.45194}, {0.177,
			   3.46321}, {0.176, 3.47454}, {0.175, 3.48594}, {
			    0.174, 3.4974}, {0.173, 3.50893}, {0.172, 3.52052}, {0.171, 3.53218}, {
			    0.17, 3.54391}, {0.169, 3.55571}, {0.168, 3.56758}, {0.167,
			   3.57952}, {0.166, 3.59153}, {0.165, 3.60362}, {0.164, 3.61578}, {0.163,
			   3.62801}, {0.162, 3.64032}, {0.161, 3.6527}, {0.16,
			    3.66516}, {0.159, 3.6777}, {0.158, 3.69032}, {0.157, 3.70302}, {0.156,
			    3.7158}, {0.155, 3.72866}, {0.154, 3.74161}, {0.153, 3.75463}, {0.152,
			    3.76775}, {0.151,
			   3.78095}, {0.15, 3.79424}, {0.149, 3.80762}, {0.148,
			    3.82109}, {0.147, 3.83465}, {0.146, 3.8483}, {0.145,
			    3.86204}, {0.144, 3.87588}, {0.143, 3.88982}, {0.142,
			    3.90386}, {0.141, 3.91799}, {0.14, 3.93223}, {0.139, 3.94656}, {0.138,
			    3.961}, {0.137, 3.97555}, {0.136, 3.9902}, {0.135, 4.00496}, {0.134,
			    4.01983}, {0.133, 4.03481}, {0.132, 4.04991}, {0.131, 4.06512}, {0.13,
			    4.08044}, {0.129, 4.09589}, {0.128, 4.11145}, {0.127, 4.12714}, {0.126,
			    4.14295}, {0.125, 4.15888}, {0.124, 4.17495}, {0.123, 4.19114}, {0.122,
			    4.20747}, {0.121, 4.22393}, {0.12, 4.24053}, {0.119, 4.25726}, {0.118,
			    4.27414}, {0.117, 4.29116}, {0.116, 4.30833}, {0.115,
			   4.32565}, {0.114, 4.34311}, {0.113,
			   4.36073}, {0.112, 4.37851}, {0.111, 4.39645}, {
			    0.11, 4.41455}, {0.109, 4.43281}, {0.108, 4.45125}, {0.107, 4.46985}, {
			    0.106, 4.48863}, {0.105, 4.50759}, {
			    0.104, 4.52673}, {0.103, 4.54605}, {0.102, 4.56556}, {0.101, 4.58527},
			{0.1, 4.60517}, {
			    0.099, 4.62527}, {0.098, 4.64558}, {0.097, 4.66609}, {0.096, 4.68681},
			{0.095, 4.70776}, {0.094, 4.72892}, {0.093, 4.75031}, {0.092, 4.77193},
			{0.091, 4.79379}, {0.09, 4.81589}, {0.089, 4.83824}, {
			  0.088, 4.86084}, {0.087, 4.88369}, {0.086, 4.90682}, {0.085, 4.93021}, {
			  0.084, 4.95388}, {0.083, 4.97783}, {0.082, 5.00207}, {0.081, 5.02661}, {
			  0.08, 5.05146}, {0.079, 5.07661}, {0.078, 5.10209}, {0.077, 5.1279}, {
			  0.076, 5.15404}, {0.075, 5.18053}, {0.074, 5.20738}, {0.073, 5.23459}, {
			  0.072, 5.26218}, {0.071, 5.29015}, {
			  0.07, 5.31852}, {0.069, 5.3473}, {0.068, 5.3765}, {
			    0.067, 5.40613}, {0.066, 5.4362}, {0.065, 5.46674}, {0.064, 5.49774}, {
			    0.063, 5.52924}, {0.062, 5.56124}, {0.061, 5.59376}, {0.06, 5.62682}, {
			    0.059, 5.66044}, {0.058, 5.69462}, {0.057, 5.72941}, {0.056, 5.76481}, {
			    0.055, 5.80084}, {0.054, 5.83754}, {0.053, 5.87493}, {0.052, 5.91302}, {
			    0.051, 5.95186}, {0.05, 5.99146}, {0.049, 6.03187}, {0.048, 6.07311}, {
			    0.047, 6.11522}, {0.046, 6.15823}, {0.045,
			    6.20219}, {0.044, 6.24713}, {0.043, 6.29311}, {0.042, 6.34017}, {0.041,
			    6.38837}, {0.04, 6.43775}, {0.039, 6.48839}, {0.038, 6.54034}, {0.037,
			    6.59367}, {0.036, 6.64847}, {0.035, 6.70481}, {0.034, 6.76279}, {0.033,
			    6.8225}, {0.032, 6.88404}, {0.031, 6.94754}, {0.03, 7.01312}, {0.029,
			    7.08092}, {0.028, 7.1511}, {0.027, 7.22384}, {0.026, 7.29932}, {0.025,
			    7.37776}, {0.024, 7.4594}, {0.023, 7.54452}, {0.022, 7.63343}, {0.021,
			7.72647}, {0.02, 7.82405}, {0.019, 7.92663}, {0.018, 8.03477}, {0.017,
			    8.14908}, {0.016, 8.27033}, {0.015, 8.39941}, {0.014, 8.5374}, {0.013,
			    8.68561}, {0.012, 8.8457}, {0.011, 9.01972}, {0.01, 9.21034}, {0.009,
			    9.42106}, {0.008, 9.65663}, {0.007, 9.92369}, {0.006, 10.232}, {0.005,
			    10.5966}, {0.004, 11.0429}, {0.003, 11.6183}, {0.002, 12.4292}, {0.001,
			    13.8155}};

        }
	public double GetPValue(double d){
		double res=0;
		for (int i=0;i<m_pdChi2Table.length-1;i++){
			if (d<m_pdChi2Table[i+1][1] && d>m_pdChi2Table[i][1]){
				res=m_pdChi2Table[i][0];
				break;
			}
		}
		return (res);
	}

	private void getJB(){
		this.myJB=data.length*((Math.pow(this.skew, 2)/6)+(Math.pow(this.kurto-3, 2)/24));
	}
}
/*
 http://www.planet-source-code.com/vb/scripts/ShowCode.asp?txtCodeId=5345&lngWId=2
 *
 Terms of Agreement:
By using this code, you agree to the following terms...

1.You may use this code in your own programs (and may compile it into a program and distribute it in compiled format for languages that allow it) freely and with no charge.
2.You MAY NOT redistribute this code (for example to a web site) without written permission from the original author. Failure to do so is a violation of copyright laws.
3.You may link to this code from another website, but ONLY if it is not wrapped in a frame.
4.You will abide by any additional copyright restrictions which the author may have placed in the code or code's description.
 */
