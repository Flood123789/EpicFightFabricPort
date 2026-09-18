package yesman.epicfight.forgecompat.client.model;

public interface IQuadTransformer {
	int STRIDE = 8;
	int POSITION = 0;
	int COLOR = 3;
	int UV0 = 4;
	int UV2 = 6;
	int NORMAL = 7;

	void processInPlace(int[] vertexData);
}
