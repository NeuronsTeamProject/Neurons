'''
→ 모델 로딩 + embed + e5_query/passsage 관련 파일입니다.
텍스트를 벡터 공간으로 옮겨주는 임베딩 과정을 전담합니다.
'''

import torch
from transformers import AutoTokenizer, AutoModel
from config import MODEL_NAME

# E5 모델 로드
tokenizer = AutoTokenizer.from_pretrained("intfloat/multilingual-e5-base")
model = AutoModel.from_pretrained("intfloat/multilingual-e5-base")

# E5권장 프롬프트 포맷 적용 헬퍼
def e5_query(text: str) -> str:
    return f"query: {text}"

def e5_passage(text: str) -> str:
    return f"passage: {text}"

# 문장을 벡터(임베딩)로 변환
def embed_text(texts):
    if isinstance(texts, str):
        texts = [texts]
    inputs = tokenizer(
        texts,
        padding=True,
        truncation=True,
        return_tensors="pt",
        max_length=512,
    )
    with torch.no_grad():
        outputs = model(**inputs)
        embeddings = outputs.last_hidden_state.mean(dim=1)
        embeddings = torch.nn.functional.normalize(embeddings, p=2, dim=1)
    return embeddings