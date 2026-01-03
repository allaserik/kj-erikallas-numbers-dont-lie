### Testing

Ensures that software works as expected by validating features against requirements. It helps catch bugs early, improves reliability, and maintains high-quality standards in development.

How to do testing?

1. Download, build, and run the submitted code.
2. Agree on your teamwork - how do you divide testing between reviewers.
3. Test functionality and check compliance with the requirements.
4. Provide feedback in the group chat and request fixes if necessary.
5. Clearly state what changes are mandatory, and what are optional fixes.
6. Repeat the testing cycle after submitters make the requested changes

#### Mandatory

### Student can explain how PII removal affects AI model's ability to generate personalized recommendations

### Student can explain their strategy for detecting and handling AI hallucinations in health recommendations

### User receives an email with verification link after registration

### Authentication options include email-password and at least 2 OAuth providers

### Password reset is handled via email

### Users can optinonally enable two-factor authentication

Verify functionality of 2FA

### User cannot access protected routes without verifying email

### Student can explain the security implications of access token duration in JWT authentication

### Access token expires after [x] minutes of inactivity

### New access token is issued when valid refresh token is provided

Verify receiving new access token by sending a request with expired access token and valid refresh token

### Platform provides clear data usage consent with explicit user approval before data collection

At minimum, it includes what data is collected and how it's used

### Platform collects basic demographics, physical metrics, lifestyle indicators, dietary preferences and restrictions, wellness goals.

### User data is encrypted in transit and at rest

### Platform collects initial fitness assessment data

### Student can explain how normalization of health metrics impacts data visualization accuracy

### Health metrics are converted to standard units before storage

### Platform allows user to change their data sharing preferences.

### Historical weight changes are tracked with timestamps

Verify each weight entry has a unique timestamp by adding multiple entries and checking their history display

### System prevents duplicate activity entries for the same timestamp

### Student can explain how BMI classifications affect wellness score calculation

### Wellness score changes when user updates their weekly activity frequency

### Student can explain their choice of AI model(s) based on response quality and latency requirements

### Student can explain what model capabilities were most important for their implementation

### System recalculates wellness score components when any contributing metric changes

Verify scores update when changing: BMI range, activity level, goal progress, or health habits

### System generates different health insights when user's goals change

Verify insight adjustment after changing user goals from one type to another (e.g., weight loss to muscle gain)

### Student can explain the difference between AI response caching and regeneration

### Student can explain how context length affects AI response quality in health recommendations

### AI recommendations include specific references to user's stated fitness goals

Verify recommendations explicitly mention and align with the specific fitness goal set in user profile

### AI health insights exclude any personally identifiable information

### Student can explain their prompt engineering approach to ensure consistent health recommendation format

### System implements response validation to filter out recommendations that don't match user's health restrictions

Ask the students to describe their approach

### Student can explain the tradeoffs between zero-shot and few-shot prompting in their implementation

### System displays cached recommendations when AI service is unavailable

Verify system shows most recent cached recommendations when AI service connection is disabled

### System generates weekly and monthly health summaries including progress and key metrics

Verify health summary includes week's activity levels, wellness score changes, and progress towards goals

### Health Dashboard shows BMI, wellness score, progress towards goals and AI insights based on user data

### Progress chart shows weight tracking over time, wellness score evolution and activity level changes

### Goal progress includes milestone tracking

Milestone example:

* Weight: every 5% towards goal
* Activity: each additional day/week
* Habits: streak achievements

### Comparison view shows current vs target metrics, weekly/monthly progress comparison, health trend analysis and AI recommendations

### Student can explain how data visualization choices affect user's understanding of progress

### AI insights are visually presented with priority-based highlighting

Verify insights are displayed with high/medium/low priority indicators and include expandable details

### Weight progress chart displays goal weight as a reference line

### Charts resize without data loss on mobile devices

### Student can explain the impact of missing health data on AI recommendation accuracy

### Error messages appear without page reload when API requests fail

Submit health profile form with invalid BMI value (e.g., -1) and verify error message appears without page refresh

### Student can explain their approach to preventing API abuse through rate limiting

### System blocks rapid-fire API requests from the same user

Verify rate limit kicks in after [x] requests within 1 minute

### Health data export includes all historical metrics and timestamps

### Student can explain the tradeoffs of their chosen data visualization library

### Dashboard loads placeholder UI when data is being fetched

### The README file contains a clear project overview, setup instructions, and usage guide

### The code is well-organized, properly commented, and follows best practices for the chosen programming language(s)

#### Extra

### Project runs entirely through Docker with a single command

The project includes a Dockerfile or script that builds and runs the app with one command. Docker is the only requirement, no manual setup or dependency installation is required.

### Quality of AI-generated health insights and progress evaluations during testing

Insights and evaluations generated are accurate, relevant, actionable

### Relevance and practicality of AI-generated weekly and monthly health summaries and recommendations

### System's handling of AI service limitations during testing (rate limits, availability)

### Student has implemented additional technologies, security enhancements and/or features beyond the core requirements
