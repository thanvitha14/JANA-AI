import React, { useState, useEffect, useRef } from "react";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";
import { MessageSquare, Send, Plus, Trash2, Scale, Loader2, ArrowLeft, Bot, User } from "lucide-react";
import Navbar from "../components/Navbar";
import { useTranslation } from "react-i18next";

const ChatPage = () => {
  const { user } = useAuth();
  const { t } = useTranslation();
  
  const [conversations, setConversations] = useState([]);
  const [activeConversationId, setActiveConversationId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState("");
  
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [sendingMessage, setSendingMessage] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const messagesEndRef = useRef(null);

  // Load user conversation list
  const fetchConversations = async () => {
    try {
      const response = await api.get("/chat/conversations");
      setConversations(response.data.data);
    } catch (e) {
      console.error("Failed to load conversations", e);
    }
  };

  useEffect(() => {
    fetchConversations();
  }, []);

  // Scroll to bottom when messages load
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, sendingMessage]);

  // Load conversation details
  const selectConversation = async (conversationId) => {
    setActiveConversationId(conversationId);
    setLoadingHistory(true);
    setMessages([]);
    try {
      const response = await api.get(`/chat/conversations/${conversationId}`);
      setMessages(response.data.data);
    } catch (e) {
      console.error("Failed to load messages", e);
    } finally {
      setLoadingHistory(false);
    }
  };

  // Start a new clean conversation
  const startNewConversation = () => {
    setActiveConversationId(null);
    setMessages([]);
    setInputMessage("");
  };

  // Send Message
  const handleSendMessage = async (e, textToSend = null) => {
    if (e) e.preventDefault();
    const finalMessageText = textToSend || inputMessage;
    
    if (!finalMessageText || finalMessageText.trim() === "") return;

    const userMsg = {
      id: Date.now().toString(), // temporary id
      userMessage: finalMessageText,
      aiResponse: "",
      createdAt: new Date().toISOString(),
    };

    // Optimistically update message list for snappy feel
    setMessages((prev) => [...prev, userMsg]);
    if (!textToSend) setInputMessage("");
    setSendingMessage(true);

    try {
      const selectedLanguage = localStorage.getItem("selectedLanguage") || "en";
      const response = await api.post("/chat", {
        message: finalMessageText,
        conversationId: activeConversationId,
        language: selectedLanguage,
      });

      const savedMessage = response.data.data;
      
      // Update with exact object containing AI response
      setMessages((prev) =>
        prev.map((m) => (m.id === userMsg.id ? savedMessage : m))
      );

      // If it was a new conversation, set active conversation id and refresh sidebar list
      if (!activeConversationId) {
        setActiveConversationId(savedMessage.conversationId);
      }
      fetchConversations();
    } catch (err) {
      console.error("Error sending message", err);
      // Fallback fallback response to user if backend fails
      setMessages((prev) =>
        prev.map((m) =>
          m.id === userMsg.id
            ? {
                ...m,
                aiResponse: t("chat.connection_error"),
              }
            : m
        )
      );
    } finally {
      setSendingMessage(false);
    }
  };

  // Delete Conversation
  const handleDeleteConversation = async (e, conversationId) => {
    e.stopPropagation(); // Avoid selecting the deleted conversation
    if (!confirm(t("chat.delete_confirm"))) return;

    try {
      await api.delete(`/chat/conversations/${conversationId}`);
      if (activeConversationId === conversationId) {
        startNewConversation();
      }
      fetchConversations();
    } catch (err) {
      console.error("Failed to delete conversation", err);
    }
  };

  // Quick Assist Suggested Prompt Click
  const handleSuggestionClick = (promptText) => {
    handleSendMessage(null, promptText);
  };

  const suggestionChips = [
    t("chat.suggested_prompts") === "Suggested Prompts" 
      ? "What legal assistance programs exist for women?" 
      : (localStorage.getItem("selectedLanguage") === "kn" 
          ? "ಮಹಿಳೆಯರಿಗೆ ಯಾವ ಕಾನೂನು ಸಹಾಯ ಕಾರ್ಯಕ್ರಮಗಳಿವೆ?" 
          : "महिलाओं के लिए कौन से कानूनी सहायता कार्यक्रम मौजूद हैं?"),
    t("chat.suggested_prompts") === "Suggested Prompts"
      ? "Show me state-wise farming subsidies and requirements"
      : (localStorage.getItem("selectedLanguage") === "kn"
          ? "ರಾಜ್ಯವಾರು ಕೃಷಿ ಸಹಾಯಧನ ಮತ್ತು ಅವಶ್ಯಕತೆಗಳನ್ನು ತೋರಿಸಿ"
          : "मुझे राज्यवार कृषि सब्सिडी और आवश्यकताएं दिखाएं"),
    t("chat.suggested_prompts") === "Suggested Prompts"
      ? "Post-matric scholarships for SC/ST college students"
      : (localStorage.getItem("selectedLanguage") === "kn"
          ? "ಪರಿಶಿಷ್ಟ ಜಾತಿ/ಪರಿಶಿಷ್ಟ ಪಂಗಡದ ಕಾಲೇಜು ವಿದ್ಯಾರ್ಥಿಗಳಿಗೆ ಮೆಟ್ರಿಕ್ ನಂತರದ ಶಿಷ್ಯವೇತನಗಳು"
          : "एससी/एसटी कॉलेज के छात्रों के लिए पोस्ट-मैट्रिक छात्रवृत्तियां"),
    t("chat.suggested_prompts") === "Suggested Prompts"
      ? "What is the Pradhan Mantri Jan Dhan Yojana?"
      : (localStorage.getItem("selectedLanguage") === "kn"
          ? "ಪ್ರಧಾನ ಮಂತ್ರಿ ಜನ ಧನ ಯೋಜನೆ ಎಂದರೇನು?"
          : "प्रधानमंत्री जन धन योजना क्या है?")
  ];

  return (
    <div className="min-h-screen bg-[#0b0c10] text-slate-100 flex flex-col h-screen">
      <Navbar />

      <div className="flex flex-1 overflow-hidden relative">
        {/* SIDEBAR */}
        <aside
          className={`${
            sidebarOpen ? "translate-x-0 w-80" : "-translate-x-full w-0"
          } transition-all duration-300 ease-in-out border-r border-white/5 bg-[#0f1015]/90 shrink-0 overflow-y-auto flex flex-col z-20 absolute md:static h-full`}
        >
          <div className="p-4 border-b border-white/5">
            <button
              onClick={startNewConversation}
              className="w-full flex items-center justify-center gap-2 btn-glass-primary py-2.5 px-4 rounded-xl text-sm font-semibold cursor-pointer"
            >
              <Plus className="h-4 w-4" /> {t("chat.new_chat")}
            </button>
          </div>

          <div className="flex-1 p-2 space-y-1.5">
            <div className="text-[10px] uppercase font-bold tracking-wider text-slate-500 px-3 py-2">
              {t("chat.chat_history")}
            </div>

            {conversations.length === 0 ? (
              <div className="text-center text-xs text-slate-500 py-8">
                {t("dashboard.no_chats")}
              </div>
            ) : (
              conversations.map((chat) => (
                <div
                  key={chat.conversationId}
                  onClick={() => selectConversation(chat.conversationId)}
                  className={`flex items-center justify-between p-3 rounded-xl cursor-pointer transition-all border ${
                    activeConversationId === chat.conversationId
                      ? "bg-brand-500/10 border-brand-500/30 text-brand-300"
                      : "border-transparent hover:bg-white/5 text-slate-400 hover:text-slate-200"
                  }`}
                >
                  <div className="flex items-center gap-2.5 min-w-0 flex-1">
                    <MessageSquare className="h-4 w-4 shrink-0" />
                    <span className="text-xs font-medium truncate">{chat.title}</span>
                  </div>
                  <button
                    onClick={(e) => handleDeleteConversation(e, chat.conversationId)}
                    className="p-1 rounded text-slate-600 hover:text-rose-400 hover:bg-rose-500/10 transition-colors cursor-pointer"
                    title={t("chat.delete_chat")}
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>
              ))
            )}
          </div>

          <div className="p-4 border-t border-white/5 text-center text-[10px] text-slate-500">
            {t("chat.core_version")}
          </div>
        </aside>

        {/* ACTIVE CHAT AREA */}
        <main className="flex-1 flex flex-col bg-[#0b0c10]/95 relative overflow-hidden">
          {/* Chat Header */}
          <div className="border-b border-white/5 py-4 px-6 flex items-center gap-3">
            <button
              onClick={() => setSidebarOpen(!sidebarOpen)}
              className="p-1.5 rounded-lg bg-white/5 border border-white/10 text-slate-300 hover:text-white md:hidden"
            >
              <ArrowLeft className="h-4 w-4" />
            </button>
            <div className="flex items-center gap-2">
              <Bot className="h-5 w-5 text-brand-500" />
              <div className="text-left">
                <h3 className="text-sm font-bold text-white leading-tight">{t("chat.title")}</h3>
                <span className="text-[10px] text-emerald-400 flex items-center gap-1 leading-none mt-0.5">
                  <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                  {t("chat.active_connected")}
                </span>
              </div>
            </div>
          </div>

          {/* Message Thread */}
          <div className="flex-1 overflow-y-auto p-6 space-y-6">
            {messages.length === 0 ? (
              <div className="h-full flex flex-col justify-center items-center max-w-xl mx-auto text-center space-y-8 animate-slide-up">
                <div className="h-16 w-16 rounded-full bg-brand-500/10 border border-brand-500/20 flex items-center justify-center text-brand-500 animate-float">
                  <Scale className="h-8 w-8" />
                </div>
                <div className="space-y-3">
                  <h2 className="text-2xl font-extrabold text-white">{t("chat.assist_title")}</h2>
                  <p className="text-slate-400 text-xs leading-relaxed">
                    {t("chat.assist_desc")}
                  </p>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full text-left">
                  {suggestionChips.map((chip, index) => (
                    <button
                      key={index}
                      onClick={() => handleSuggestionClick(chip)}
                      className="glass-panel glass-panel-hover p-4 rounded-xl text-xs text-slate-300 font-medium hover:text-white flex items-center justify-between cursor-pointer"
                    >
                      <span>{chip}</span>
                      <Send className="h-3 w-3 text-brand-500 shrink-0 ml-2" />
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              <div className="max-w-3xl mx-auto space-y-6">
                {loadingHistory ? (
                  <div className="flex justify-center py-12">
                    <Loader2 className="h-8 w-8 text-brand-500 animate-spin" />
                  </div>
                ) : (
                  messages.map((msg) => (
                    <div key={msg.id} className="space-y-4">
                      {/* User Message (Right Side) */}
                      {msg.userMessage && (
                        <div className="flex items-start justify-end gap-3">
                          <div className="flex flex-col items-end max-w-[85%] sm:max-w-[70%]">
                            <div className="bg-brand-600 border border-brand-500/30 text-white rounded-2xl rounded-tr-none py-3 px-4.5 text-xs shadow-lg text-left">
                              <p className="whitespace-pre-line">{msg.userMessage}</p>
                            </div>
                            <span className="text-[9px] text-slate-500 mt-1">
                              {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          </div>
                          <div className="h-8 w-8 rounded-full bg-brand-900 border border-brand-700/50 flex items-center justify-center shrink-0">
                            <User className="h-4 w-4 text-brand-200" />
                          </div>
                        </div>
                      )}

                      {/* AI Response (Left Side) */}
                      {msg.aiResponse ? (
                        <div className="flex items-start gap-3">
                          <div className="h-8 w-8 rounded-full bg-slate-900 border border-white/10 flex items-center justify-center shrink-0">
                            <Bot className="h-4 w-4 text-emerald-400" />
                          </div>
                          <div className="flex flex-col items-start max-w-[85%] sm:max-w-[75%]">
                            <div className="glass-panel text-slate-200 rounded-2xl rounded-tl-none py-3 px-4.5 text-xs shadow-md text-left border border-white/5 space-y-2">
                              {msg.aiResponse.split("\n\n").map((para, pIdx) => {
                                // Formatting markdown lists and bold text simply
                                if (para.startsWith("###")) {
                                  return <h4 key={pIdx} className="font-bold text-white text-sm mt-2">{para.replace("###", "").trim()}</h4>;
                                }
                                if (para.startsWith("1.") || para.startsWith("-")) {
                                  return (
                                    <ul key={pIdx} className="list-disc pl-4 space-y-1 my-1">
                                      {para.split("\n").map((line, lIdx) => (
                                        <li key={lIdx} className="list-item">
                                          {line.replace(/^-\s*|^\d+\.\s*/, "").replace(/\*\*(.*?)\*\*/g, "$1")}
                                        </li>
                                      ))}
                                    </ul>
                                  );
                                }
                                // Simple bold highlights
                                return (
                                  <p key={pIdx} className="leading-relaxed">
                                    {para.split("**").map((textPart, tIdx) => 
                                      tIdx % 2 === 1 ? <strong key={tIdx} className="text-white font-semibold">{textPart}</strong> : textPart
                                    )}
                                  </p>
                                );
                              })}
                            </div>
                            <span className="text-[9px] text-slate-500 mt-1">
                              {t("chat.jana_response")}
                            </span>
                          </div>
                        </div>
                      ) : (
                        // If it's sending, show a loading placeholder bubble
                        sendingMessage && msg.id === messages[messages.length - 1].id && (
                          <div className="flex items-start gap-3">
                            <div className="h-8 w-8 rounded-full bg-slate-900 border border-white/10 flex items-center justify-center shrink-0">
                              <Bot className="h-4 w-4 text-brand-500" />
                            </div>
                            <div className="glass-panel text-slate-400 rounded-2xl rounded-tl-none py-3 px-4.5 text-xs shadow-md border border-white/5 flex items-center gap-2">
                              <Loader2 className="h-3.5 w-3.5 animate-spin text-brand-500" />
                              <span>{t("common.loading")}</span>
                            </div>
                          </div>
                        )
                      )}
                    </div>
                  ))
                )}
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input Bar */}
          <div className="p-4 border-t border-white/5 bg-[#0f1015]/80">
            <form onSubmit={handleSendMessage} className="max-w-3xl mx-auto flex items-center gap-3">
              <input
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                placeholder={t("chat.input_placeholder")}
                className="flex-1 glass-input py-3 text-xs"
                disabled={sendingMessage}
              />
              <button
                type="submit"
                disabled={sendingMessage || inputMessage.trim() === ""}
                className="p-3 rounded-xl btn-glass-primary flex items-center justify-center cursor-pointer disabled:opacity-50"
              >
                {sendingMessage ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Send className="h-4 w-4" />
                )}
              </button>
            </form>
          </div>
        </main>
      </div>
    </div>
  );
};

export default ChatPage;
