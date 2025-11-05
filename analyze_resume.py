import pdfplumber   # pdf 텍스트 추출
import nltk
import torch
import re           # 억지매칭/노이즈 문장 해결을 위한 임포트 추가
from transformers import AutoTokenizer, AutoModel


# 초기 설정
# nltk 리소스 다운로드 (최초 1회만 주석 해제해서 실행)
# nltk.download('punkt')
# nltk.download('punkt_tab')

# E5 모델 로드
tokenizer = AutoTokenizer.from_pretrained("intfloat/multilingual-e5-base")
model = AutoModel.from_pretrained("intfloat/multilingual-e5-base")

# 기술/역량 키워드 및 점수
technical_keywords = {
    "BACKEND": {
        "리더십": 2.0,
        "주도적": 2.0,
        "추진력": 2.0,
        "도전적": 2.0,
        "갈등 및 의견 조율": 2.0,
        "갈등해결": 4.0,
        "팀워크": 4.0,
        "데이터베이스": 4.0,
        "python": 4.0,
        "java": 4.0,
        "ssafy": 6.0,
        "의견조율": 6.0,
        "팀장": 6.0,
        "관리감독": 6.0,
        "개발프로젝트": 6.0,
    },
}

# 키워드와 문장의 정확한 매칭을 위해 유사도를 올리기 위한 키워드 정의
KEYWORD_TEMPLATES = {
    "리더십": [
        "팀의 목표를 설정하고 구성원들이 방향성을 잃지 않도록 이끌었습니다.",
        "프로젝트 진행 중 팀의 사기를 높이고 동료들을 독려했습니다.",
        "팀 내 역할을 명확히 분배하고 업무 진척도를 관리했습니다.",
        "위기 상황에서도 침착하게 팀을 통솔하며 문제를 해결했습니다.",
        "프로젝트 리더로서 주요 의사결정을 주도했습니다.",
        "팀원 간의 갈등을 중재하고 협력을 유도했습니다.",
        "목표 달성을 위해 팀의 역량을 극대화할 수 있는 전략을 수립했습니다.",
        "주도적으로 회의를 이끌고 의사소통의 중심 역할을 했습니다.",
        "구성원의 의견을 존중하며 팀 전체가 성장할 수 있도록 지원했습니다.",
        "성과 공유와 피드백 문화 정착을 위해 주기적인 리뷰 세션을 운영했습니다.",
    ],
    "주도적": [
        "주어진 업무를 수동적으로 수행하지 않고 스스로 개선안을 제시했습니다.",
        "문제를 발견하면 즉시 대안을 찾고 실행에 옮겼습니다.",
        "새로운 기술 도입이 필요하다고 판단하여 직접 연구하고 적용했습니다.",
        "타 부서 협조가 필요할 때 먼저 소통하며 업무를 진행했습니다.",
        "업무 일정과 목표를 스스로 관리하며 책임감을 가지고 수행했습니다.",
        "프로젝트 초기 단계부터 요구사항을 선제적으로 분석했습니다.",
        "개선점이 보이면 보고만 하지 않고 직접 수정 및 반영했습니다.",
        "리스크를 예측하고 사전에 대응 방안을 마련했습니다.",
        "동료들이 어려움을 겪을 때 먼저 나서서 해결을 도왔습니다.",
        "효율성을 높이기 위해 자발적으로 자동화 도구를 개발했습니다."
    ],
    "추진력": [
        "목표를 설정한 뒤 끝까지 완수하기 위해 계획적으로 실행했습니다.",
        "일정이 촉박한 상황에서도 팀을 이끌며 과제를 제시간에 완료했습니다.",
        "새로운 프로젝트를 시작할 때 주도적으로 추진했습니다.",
        "예상치 못한 문제에도 멈추지 않고 해결 방안을 끝까지 모색했습니다.",
        "결과 도출을 위해 다양한 대안을 시도하며 추진했습니다.",
        "어려운 업무라도 끝까지 밀어붙이는 강한 실행력을 보였습니다.",
        "목표 달성을 위해 필요한 리소스를 직접 확보했습니다.",
        "의사결정 후 신속하게 실행 단계로 전환했습니다.",
        "초기 계획이 틀어져도 중도 포기하지 않고 완성했습니다.",
        "팀 전체의 속도를 높이기 위해 진행상황을 지속적으로 점검했습니다."
    ],
    "도전적": [
        "익숙하지 않은 기술에도 두려움 없이 도전했습니다.",
        "새로운 환경에서도 빠르게 적응하며 문제를 해결했습니다.",
        "기존 방식을 개선하기 위해 새로운 방법론을 실험했습니다.",
        "기술적 난이도가 높은 과제에 자발적으로 참여했습니다.",
        "목표가 어렵더라도 포기하지 않고 끈기 있게 수행했습니다.",
        "처음 시도하는 프레임워크를 도입하여 성과를 냈습니다.",
        "한정된 자원 속에서도 새로운 해결책을 찾아냈습니다.",
        "고객의 까다로운 요구사항에 맞춰 기능을 구현했습니다.",
        "실패를 두려워하지 않고 계속해서 시도했습니다.",
        "더 나은 성과를 위해 기존 시스템을 전면 개편했습니다."
    ],
    "갈등 및 의견 조율": [
        "팀 내 의견 차이를 조율하여 모두가 동의할 수 있는 결론을 도출했습니다.",
        "회의 중 발생한 갈등을 중립적으로 중재했습니다.",
        "서로 다른 부서 간의 이해관계를 조정했습니다.",
        "프로젝트 방향성에 대한 이견을 조율하여 일관된 전략을 수립했습니다.",
        "문제의 원인을 분석하고 구성원 간의 소통을 촉진했습니다.",
        "상대방의 입장을 경청하고 합리적인 합의를 이끌었습니다.",
        "다양한 의견을 수렴하여 최적의 의사결정을 도출했습니다.",
        "협업 중 발생한 오해를 해소하기 위해 미팅을 주도했습니다.",
        "의사소통 방식을 개선해 팀 내 협력을 강화했습니다.",
        "갈등이 생겼을 때 감정보다는 데이터 기반으로 해결했습니다."
    ],
    "갈등해결": [
        "의견 충돌이 발생했을 때 객관적 기준으로 문제를 해결했습니다.",
        "팀원 간 갈등을 조정하고 긍정적인 분위기를 조성했습니다.",
        "협업 과정에서 발생한 갈등 상황을 직접 중재했습니다.",
        "구성원 간의 오해를 풀고 팀워크를 회복시켰습니다.",
        "불만 사항을 청취하고 개선책을 제시했습니다.",
        "문제의 본질을 파악하고 관련자들과 소통해 합의를 이끌었습니다.",
        "갈등이 장기화되지 않도록 즉각 대응했습니다.",
        "감정보다는 사실과 데이터에 기반해 문제를 해결했습니다.",
        "상호 존중을 바탕으로 문제를 원만히 마무리했습니다.",
        "갈등을 계기로 더 나은 협업 프로세스를 구축했습니다."
    ],
    "팀워크": [
        "팀원들과 긴밀히 협력하여 공동의 목표를 달성했습니다.",
        "협업 도중 어려운 부분은 서로 보완하며 진행했습니다.",
        "다양한 의견을 수용해 더 나은 결과를 만들어냈습니다.",
        "동료들과 원활히 소통하며 프로젝트를 완성했습니다.",
        "역할을 분담하고 각자의 강점을 살려 성과를 냈습니다.",
        "문제 발생 시 함께 원인을 분석하고 해결책을 찾았습니다.",
        "협업 효율을 높이기 위해 코드 리뷰 문화를 도입했습니다.",
        "회의와 피드백을 통해 팀 전체가 성장할 수 있도록 기여했습니다.",
        "상호 신뢰를 바탕으로 일의 완성도를 높였습니다.",
        "다른 부서와 협력해 목표를 공동 달성했습니다."
    ],
    "데이터베이스": [
        "데이터베이스 테이블 구조를 직접 설계했습니다.",
        "SQL을 작성해 데이터 조회 및 통계를 산출했습니다.",
        "인덱스와 조인을 최적화하여 성능을 개선했습니다.",
        "MySQL을 기반으로 CRUD API를 구현했습니다.",
        "DB 스키마 변경 및 마이그레이션을 수행했습니다.",
        "ORM을 활용해 데이터 접근 로직을 단순화했습니다.",
        "데이터 무결성을 보장하기 위한 트랜잭션을 관리했습니다.",
        "Redis를 캐시로 활용해 조회 속도를 향상시켰습니다.",
        "RDB와 NoSQL을 비교하여 시스템에 적합한 구조를 선택했습니다.",
        "데이터베이스 장애 발생 시 백업 데이터를 통해 복구했습니다."
    ],
    "Python": [
        "파이썬으로 데이터 전처리 및 자동화 스크립트를 작성했습니다.",
        "Django를 활용해 웹 애플리케이션을 개발했습니다.",
        "Flask 기반 REST API 서버를 구현했습니다.",
        "Pandas와 NumPy를 사용해 데이터 분석을 수행했습니다.",
        "Selenium으로 테스트 자동화 스크립트를 개발했습니다.",
        "파이썬으로 크롤링 프로그램을 작성했습니다.",
        "업무 효율화를 위한 파이썬 자동화 툴을 제작했습니다.",
        "파이썬 기반 AI/ML 모델 학습 및 추론을 진행했습니다.",
        "PyTorch를 활용해 텍스트 분류 모델을 구현했습니다.",
        "파이썬 스크립트를 통해 로그 수집 및 처리 시스템을 구축했습니다."
    ],
    "Java": [
        "스프링 부트를 사용해 백엔드 API를 개발했습니다.",
        "자바로 MVC 구조의 웹 애플리케이션을 구축했습니다.",
        "JPA를 이용해 데이터베이스 연동 로직을 작성했습니다.",
        "RESTful 서비스를 자바 기반으로 구현했습니다.",
        "테스트 코드를 작성해 안정성을 확보했습니다.",
        "스프링 시큐리티를 적용해 인증·인가 로직을 개발했습니다.",
        "서버 성능 향상을 위해 쓰레드풀을 조정했습니다.",
        "기존 레거시 자바 코드를 리팩토링했습니다.",
        "자바 스트림을 이용해 데이터를 효율적으로 처리했습니다.",
        "자바 기반 마이크로서비스 환경에서 통신 모듈을 개발했습니다."
    ],
    "SSAFY": [
        "삼성 청년 SW 아카데미(SSAFY)에서 프로젝트를 수행했습니다.",
        "SSAFY 교육 과정을 통해 알고리즘과 웹 개발 역량을 강화했습니다.",
        "SSAFY 팀 프로젝트에서 프론트엔드 개발을 담당했습니다.",
        "SSAFY 수료 후 다양한 기술 스택을 실무에 적용했습니다.",
        "SSAFY에서 배운 협업 및 버전 관리 역량을 현업에 활용했습니다.",
        "SSAFY 과정 중 Python, Java 기반 프로젝트를 수행했습니다.",
        "SSAFY에서 Agile 기반 협업을 경험했습니다.",
        "SSAFY에서 발표 및 피드백 중심의 팀 문화에 참여했습니다.",
        "SSAFY 해커톤에 참가하여 문제 해결 역량을 검증받았습니다.",
        "SSAFY 수료 인증 및 포트폴리오를 작성했습니다."
    ],
    "의견조율": [
        "여러 이해관계자 간의 의견 차이를 조율했습니다.",
        "팀 내 의사결정 과정에서 합의를 이끌어냈습니다.",
        "회의 중 대립된 의견을 균형 있게 조정했습니다.",
        "서로 다른 부서의 요구사항을 조율했습니다.",
        "논의 중 감정 대립을 완화하고 논리적으로 설득했습니다.",
        "의사소통을 통해 프로젝트 방향성을 일치시켰습니다.",
        "고객의 요구와 팀의 한계를 고려해 현실적인 대안을 제시했습니다.",
        "상대방의 의견을 경청하고 객관적인 판단을 내렸습니다.",
        "중립적인 입장에서 팀의 결정을 조율했습니다.",
        "여러 제안을 조합하여 최적의 결과를 도출했습니다."
    ],
    "팀장": [
        "팀장으로서 프로젝트 일정을 관리했습니다.",
        "팀 내 역할을 분배하고 성과를 점검했습니다.",
        "구성원들의 의견을 수렴하고 최종 결정을 내렸습니다.",
        "회의를 주관하며 진행 상황을 공유했습니다.",
        "문제 발생 시 팀 전체를 조정했습니다.",
        "후배 개발자에게 코드 리뷰와 피드백을 제공했습니다.",
        "팀 목표를 설정하고 업무 방향을 제시했습니다.",
        "팀원 간 협업 문화를 조성했습니다.",
        "상급자와 팀원 간의 소통을 중재했습니다.",
        "팀의 성과를 대표하여 보고 및 발표를 수행했습니다."
    ],
    "관리감독": [
        "프로젝트 일정과 품질을 관리했습니다.",
        "진행 상황을 점검하고 리스크를 사전에 파악했습니다.",
        "업무 효율을 높이기 위해 프로세스를 개선했습니다.",
        "구성원의 업무 수행 상태를 주기적으로 리뷰했습니다.",
        "인력과 자원을 최적화하여 생산성을 향상시켰습니다.",
        "업무 결과를 검토하고 피드백을 제공했습니다.",
        "계획 대비 실적을 관리하며 일정 지연을 방지했습니다.",
        "외주 인력의 작업 품질을 감독했습니다.",
        "팀 전체의 퍼포먼스를 관리했습니다.",
        "품질 기준을 수립하고 준수 여부를 점검했습니다."
    ],
    "개발프로젝트": [
        "실제 사용자 대상의 개발 프로젝트를 진행했습니다.",
        "팀 기반으로 웹 서비스 개발 프로젝트를 수행했습니다.",
        "요구사항 분석부터 배포까지 전 과정을 담당했습니다.",
        "프론트엔드와 백엔드를 연동하는 프로젝트를 진행했습니다.",
        "새로운 기술 스택을 활용한 개발 프로젝트를 수행했습니다.",
        "클라이언트 요구에 맞춘 맞춤형 프로젝트를 구현했습니다.",
        "프로젝트 산출물로 완성된 웹 애플리케이션을 배포했습니다.",
        "협업 툴을 활용해 프로젝트 진행 상황을 관리했습니다.",
        "개발 중 발생한 이슈를 트래킹하고 해결했습니다.",
        "기획자, 디자이너, 개발자가 함께한 프로젝트를 완수했습니다."
    ]
    # 추후에 필요한 키워드 추가
}

# 자격/어학 전용 키워드(의미 유사도를 고려하지 않고 문자열 매칭으로 점수 부여)
cert_keywords = {
    "정보처리기사": 4.0,
    "빅데이터분석기사": 4.0,
    "SQLD": 2.0,
    "정보보안기사": 5.0,
    "TOEIC": 3.0,
    "토익": 3.0,
    "OPIC": 3.0,
    "오픽": 3.0,
    "JLPT": 3.0,
    "TOPIK": 3.0,
}

# 섹션 의미 정의(어떤 내용을 포함해야 해당 섹션으로 분류될지 텍스트로 정의합니다.)
section_labels = {
    "자기소개": (
        "이 섹션은 지원자의 성격, 가치관, 강점과 약점, 성장 배경, 인생관 등을 설명하는 자기소개 부분입니다. "
        "예를 들어, ‘저는 성실함과 책임감을 바탕으로 맡은 일은 끝까지 수행합니다’, ‘새로운 도전을 두려워하지 않고 항상 배우려는 자세로 임합니다’ "
        "와 같이 본인의 태도, 인성, 일하는 방식, 조직에 대한 자세 등을 기술하는 문장들이 포함됩니다. "
        "또한 자신이 중요하게 생각하는 가치, 팀 내에서의 역할, 일에 임하는 철학이나 동기를 서술하는 경우도 해당됩니다. "
        "즉, 자기 자신을 소개하거나 자신의 성향을 설명하는 내용이 중심이 되는 문장입니다."
    ),

    "경험사례": (
        "이 섹션은 지원자가 과거에 겪은 구체적인 경험이나 성취 사례를 중심으로 서술하는 부분입니다. "
        "예를 들어, ‘개발 프로젝트에서 발생한 문제를 직접 분석하고 해결했습니다’, ‘목표를 달성하기 위해 팀원들과 전략을 세웠습니다’ "
        "와 같이 구체적인 상황, 행동, 결과를 포함하는 문장들이 여기에 해당합니다. "
        "문제 해결, 도전 경험, 성취 사례, 프로젝트 수행 과정 등 과거의 실제 경험을 통해 자신의 역량을 드러내는 문장들이 중심이 됩니다. "
        "즉, ‘무엇을 했는가’, ‘어떤 문제를 해결했는가’, ‘어떤 결과를 냈는가’를 구체적으로 설명하는 문장입니다."
    ),

    "협업경험": (
        "이 섹션은 팀워크, 소통, 협력, 리더십 등 타인과의 관계 속에서 발휘된 역량을 서술하는 부분입니다. "
        "예를 들어, ‘팀원 간의 의견 차이를 조율했습니다’, ‘프로젝트 리더로서 팀원들을 이끌었습니다’, ‘회의를 통해 문제를 함께 해결했습니다’ "
        "와 같이 협업 과정에서의 역할, 의사소통 방식, 갈등 해결, 상호 협력 경험을 구체적으로 설명하는 문장들이 포함됩니다. "
        "팀 내 역할 분담, 의견 조율, 회의, 리더십 발휘, 피드백, 동료 지원 등 사람 간 상호작용이 핵심인 문장들이 중심이 됩니다. "
        "즉, 다른 사람과 함께 일하며 발생한 상황이나 경험을 중심으로 한 내용이 여기에 해당합니다."
    ),

    "자격어학": (
        "이 섹션은 지원자의 보유 자격증, 어학 성적, 취득 일자, 공인 시험 결과 등을 나열하거나 기술하는 부분입니다. "
        "예를 들어, ‘정보처리기사 자격증 취득’, ‘TOEIC 900점’, ‘JLPT N1’, ‘SQLD 자격증 합격’ 등과 같이 정량적이고 객관적인 스펙을 명시하는 문장들이 포함됩니다. "
        "또한 ‘자격증’, ‘점수’, ‘취득’, ‘수료’, ‘인증’과 같은 단어가 포함된 경우 대부분 이 섹션에 해당합니다. "
        "즉, 자기소개나 경험 서술이 아닌, 자신이 갖춘 공식적인 역량 지표(자격, 어학, 교육 이수 등)를 나열하는 문장입니다."
    ),
}

# 유사도 임계값
THRESHOLD_SECTION = 0.7   # 섹션 분류 시 신뢰 기준
THRESHOLD_KEYWORD = 0.7   # 키워드(역량) 매칭 인정 기준


# (2) 유틸 함수---------------------------------------------------------------------
# E5권장 프롬프트 포맷 적용 헬퍼
def e5_query(text: str) -> str:
    return f"query: {text}"

def e5_passage(text: str) -> str:
    return f"passage: {text}"

# 쓰레기 문장 필터함수
def is_informative_sentence(sentence: str) -> bool:
    s = sentence.strip()
    # 매칭된 문장이 너무 짧으면 버리기
    if len(s) < 8:
        return False
    # 숫자/기호만 있는 경우 버리기
    if re.fullmatch(r"^[\d\-\.\)\(※\*]+$", s):
        return False
    # 전형적인 양식/서약/관리 문구 버리기
    boilerplate_patterns = [
        "사실에 근거하여 성실히 작성하였음을 서약합니다",
        "사실과 다름없음을 확인합니다",
        "위 내용은 사실과 다름없습니다",
        "본인은 이에 동의합니다",
        "개인정보의 수집 및 이용에 동의합니다",
    ]
    for pat in boilerplate_patterns:
        if pat in s:
            return False
    return True

# PDF 텍스트 추출
def extract_text_from_pdf(pdf_path: str) -> str:
    text = ""
    with pdfplumber.open(pdf_path) as pdf:
        for page in pdf.pages:
            content = page.extract_text()
            if content:
                text += content + "\n"
    return text.strip()

# 문장 분리
def split_sentences(text: str):
    return nltk.sent_tokenize(text)

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


# (3) 섹션 분류------------------------------------------------------------

# 자격/어학 성격의 문장인지 먼저 강하게 체크
def rule_based_precheck(sentence: str):
    # 자격/어학에 등장할 법한 단어가 하나라도 있으면 무조건 '자격어학'으로 분류
    if any(word in sentence for word in [
        "정보처리기사", "빅데이터분석기사", "정보보안기사",
        "SQLD", "TOEIC", "토익", "OPIC", "오픽", "JLPT", "TOPIK",
        "자격증", "어학", "점수"
    ]):
        return "자격어학"
    return None

# 문장을 섹션으로 자동 분류
def classify_sentences_by_section(sentences, section_labels, embed_text):
    section_names = list(section_labels.keys())
    section_descs = list(section_labels.values())

    # 각 섹션 의미 임베딩, 문장 임베딩
    section_emb = embed_text(section_descs)    # [num_sections, dim]
    sentence_emb = embed_text(sentences)       # [num_sentences, dim]

    # 유사도 행렬 (문장 x 섹션)
    similarity = sentence_emb @ section_emb.T  # [num_sentences, num_sections]

    # 결과 버킷
    section_buckets = {name: [] for name in section_names}

    for i, row in enumerate(similarity):
        sent = sentences[i]

        # 1) 룰 기반으로 '자격어학' 강제 분류 가능한지 먼저 확인
        forced_section = rule_based_precheck(sent)
        if forced_section:
            section_buckets[forced_section].append(sent)
            continue

        # 2) 룰에 안 걸리면 E5로 의미적 섹션 분류
        best_idx = torch.argmax(row).item()
        best_section = section_names[best_idx]
        best_score = float(row[best_idx])  # 문장 ↔ 해당 섹션 설명문 유사도

        # 강조되는 내용은 섹션으로 인식
        if best_score >= THRESHOLD_SECTION:
            section_buckets[best_section].append(sent)

    return section_buckets


# (4) 점수 계산--------------------------------------------------------------

# A. 자격어학 섹션 점수 계산 (키워드 포함 여부만) - 쓰레기 문장 필터는 굳이 추가X
def score_section_cert(sentences, cert_keywords, section_name, awarded_keywords_global):
    section_total_score = 0.0
    match_log = []

    for sent in sentences:
        for kw, kw_score in cert_keywords.items():
            if kw in sent:
                # 한 번 매칭된 자격증은 다시 매칭 제한
                if kw in awarded_keywords_global:
                    continue
                awarded_keywords_global.add(kw)

                section_total_score += kw_score
                match_log.append({
                    "section": section_name,
                    "sentence": sent,       # 키워드가 매칭된 근거문장 저장
                    "keyword": kw,
                    "sim": None,          # 의미 유사도 안 씀
                    "score": kw_score,    # 그대로 점수 부여
                })

    return section_total_score, match_log

# B. 일반 섹션 점수 계산 (의미 유사도 기반)
def score_section_semantic(sentences, keywords_dict, embed_text, section_name, awarded_keywords_global):
    # 1) 의미 없는 문장 제거
    filtered_sentences = [s for s in sentences if is_informative_sentence(s)]

    section_total_score = 0.0
    match_log = []

    # 2) 필터 후 문장이 하나도 없으면 그냥 0점 리턴
    if not filtered_sentences:
        return section_total_score, match_log

    # 3) 문장 임베딩
    passage_texts = [e5_passage(s) for s in filtered_sentences]
    sentence_emb = embed_text(passage_texts)  # shape: [num_sentences, dim]

    canonical_keywords = []      # ["python","팀워크",...]
    template_texts = []          # ["query: 파이썬을 ...", ...]
    template_owner = []          # 템플릿이 속한 정식 키워드명

    for _, kw_data in keywords_dict.items():
        for kw in kw_data.keys():
            # 해당 키워드에 등록된 템플릿이 있으면 사용, 없으면 키워드 자체를 문장화
            templates = KEYWORD_TEMPLATES.get(kw, [f"{kw}와 관련된 경험이 있다"])
            for t in templates:
                canonical_keywords.append(kw)
                template_texts.append(e5_query(t))
                template_owner.append(kw)

    template_emb = embed_text(template_texts)  # [num_templates, dim]

    # ③ 문장 x 템플릿 유사도
    #    이후 "정식 키워드" 단위로 최대값을 집계
    sim_mat = sentence_emb @ template_emb.T    # [S x T]

    # ④ 정식 키워드별 최대 유사도를 계산하기 위해, 템플릿 인덱스를 묶어둠
    from collections import defaultdict
    owner_to_indices = defaultdict(list)
    for idx, owner in enumerate(template_owner):
        owner_to_indices[owner].append(idx)

    # ⑤ 각 문장에 대해 "정식 키워드" 별 최대 유사도 계산 → best_kw 선택
    for sent_idx in range(sim_mat.shape[0]):
        # 키워드별 최대 유사도 계산
        best_kw = None
        best_sim = -1.0
        best_template_idx = None

        row = sim_mat[sent_idx]  # [T]

        for kw, indices in owner_to_indices.items():
            # 이 키워드의 모든 템플릿에 대한 최대 유사도
            sims = row[indices]
            kw_max = float(torch.max(sims))
            if kw_max > best_sim:
                best_sim = kw_max
                best_kw = kw
                # 최대가 나온 템플릿 인덱스도 찾기 (근거용)
                local_best = torch.argmax(sims).item()
                best_template_idx = indices[local_best]

        # ⑥ 임계값 통과 + 중복 방지 후 점수 부여
        if best_sim >= THRESHOLD_KEYWORD and best_kw not in awarded_keywords_global:
            # 점수표에서 스코어 가져오기 (첫 카테고리에서 찾음)
            kw_score = None
            for _, kw_data in keywords_dict.items():
                if best_kw in kw_data:
                    kw_score = kw_data[best_kw]
                    break
            if kw_score is None:
                continue  # 안전장치

            awarded_keywords_global.add(best_kw)
            section_total_score += kw_score

            # 근거: 문장 + 사용된 템플릿(사실상 '의미 설명') 표시
            match_log.append({
                "section": section_name,
                "sentence": filtered_sentences[sent_idx],
                "keyword": best_kw,
                "sim": round(best_sim, 3),
                "score": kw_score,
                "used_template": template_texts[best_template_idx],  # 디버깅/설명용
            })

    return section_total_score, match_log



# (5) 전체 분석--------------------------------------------------------------

def analyze_resume(pdf_path: str):
    # 1) PDF → 전체 텍스트 추출 → 문장 분리
    text = extract_text_from_pdf(pdf_path)
    sentences = split_sentences(text)

    # 2) 문장들을 의미적으로 섹션에 분류
    section_buckets = classify_sentences_by_section(
        sentences,
        section_labels,
        embed_text
    )

    total_sum = 0.0
    match_log = []

    awarded_keywords_global = set()     # 전역 키워드 중복 장지 세트

    # 3) 섹션별 점수 계산
    for section_name, section_sentences in section_buckets.items():
        if not section_sentences:
            continue

        if section_name == "자격어학":
            # [자격/어학] - 키워드 포함 여부로 판단
            section_score, matches = score_section_cert(
                section_sentences,
                cert_keywords,
                section_name,
                awarded_keywords_global
            )
        else:
            # [자기소개/경험사례/협업경험] - 의미 유사도 기반으로 판단
            section_score, matches = score_section_semantic(
                section_sentences,
                technical_keywords,
                embed_text,
                section_name,
                awarded_keywords_global
            )

        total_sum += section_score
        match_log.extend(matches)

    # 4) 결과 리턴
    return {
        "total_sum": total_sum,
        "matches": match_log
    }


# 분석결과 출력

if __name__ == "__main__":
    pdf_file = "resume.pdf"  # 분석하려는 PDF 파일명
    result = analyze_resume(pdf_file)

    print("\n[요약 결과]")
    print(f"총합 점수: {result['total_sum']}\n")

    print("🔍 [매칭된 키워드 요약]")
    for match in result["matches"]:
        section = match["section"]
        keyword = match["keyword"]
        sim = match["sim"]
        score = match["score"]
        sentence = match["sentence"]

        # 유사도가 None인 경우(자격어학 룰 기반)는 'N/A'로 처리
        sim_display = "N/A" if sim is None else sim

        print(f" - [{section}] '{keyword}' (유사도: {sim_display}, 점수부여: {score})")
        print(f"    ↳ 근거 문장: \"{sentence}\"")
